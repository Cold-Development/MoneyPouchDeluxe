#!/usr/bin/env python3
"""Announce a published GitHub release on Discord as a Components V2 message.

Runs from .github/workflows/discord-release.yml. Everything comes from the environment:
  DISCORD_WEBHOOK  the webhook URL (repo secret - never printed)
  PURCHASE_URL     where the "Purchase" button points
  PLUGIN_NAME      display name in the heading
  TAG              release tag, e.g. v2.0.1 - optional on a manual run
  RELEASE_URL      html_url of the release; when empty the release is looked up instead
  RELEASE_BODY     the release notes as written on GitHub
  DRY_RUN          print the payload and post nothing

On a release event the workflow passes TAG/RELEASE_URL/RELEASE_BODY straight from the
payload. On a manual run they are absent, so the release is fetched from the GitHub API -
by TAG if one was given, otherwise the newest one.
"""
import json
import os
import re
import sys
import urllib.error
import urllib.request

ACCENT = 0x57F287          # same green the Auxilor notifier uses
CHANGELOG_LIMIT = 1200     # a TextDisplay takes up to 4000 chars; stay well under
MAX_LINES = 10


def truthy(value):
    return str(value or "").strip().lower() in ("1", "true", "yes", "on")


def fetch_release(repo, tag, token):
    """Look a release up when the workflow was started by hand rather than by a release."""
    path = "releases/tags/%s" % urllib.parse.quote(tag) if tag else "releases/latest"
    headers = {"Accept": "application/vnd.github+json",
               "User-Agent": "mpd-release-notify/1.0"}
    if token:
        headers["Authorization"] = "Bearer " + token
    req = urllib.request.Request("https://api.github.com/repos/%s/%s" % (repo, path),
                                 headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            data = json.load(resp)
    except urllib.error.HTTPError as e:
        hint = " - no published release yet?" if e.code == 404 else ""
        sys.exit("notify: GitHub returned HTTP %d for %s%s" % (e.code, path, hint))
    except urllib.error.URLError as e:
        sys.exit("notify: could not reach the GitHub API: %s" % e.reason)

    if data.get("draft"):
        sys.exit("notify: %s is still a draft - nothing to announce" % data.get("tag_name"))
    return (data.get("tag_name") or "", data.get("html_url") or "", data.get("body") or "")


def env(name, required=True):
    v = os.environ.get(name, "").strip()
    if not v and required:
        sys.exit("notify: missing required env var %s" % name)
    return v


def mini_changelog(body):
    """Trim GitHub's release notes down to a short, readable summary."""
    if not body:
        return "_Fara detalii in release notes._"

    lines = []
    for raw in body.replace("\r\n", "\n").split("\n"):
        line = raw.rstrip()
        # GitHub's generated notes end with their own full-changelog link, and we add our
        # own below - drop theirs so the message does not carry two.
        if re.match(r"^\*\*Full Changelog\*\*:", line, re.I):
            continue
        # "## What's Changed" is a heading that would render huge next to our own h2.
        if re.match(r"^#{1,6}\s", line):
            line = "**%s**" % re.sub(r"^#{1,6}\s+", "", line)
        lines.append(line)

    # collapse the blank lines the trimming leaves behind
    out, blank = [], False
    for line in lines:
        if not line.strip():
            blank = True
            continue
        if blank and out:
            out.append("")
        blank = False
        out.append(line)

    truncated = len(out) > MAX_LINES
    out = out[:MAX_LINES]
    text = "\n".join(out).strip()

    if len(text) > CHANGELOG_LIMIT:
        text = text[:CHANGELOG_LIMIT].rsplit("\n", 1)[0].rstrip()
        truncated = True
    if truncated:
        text += "\n…"
    return text or "_Fara detalii in release notes._"


def build_payload(plugin, tag, release_url, body, purchase_url):
    version = tag if tag.lower().startswith("v") else "v" + tag
    return {
        "flags": 32768,  # IS_COMPONENTS_V2
        "components": [
            {
                "type": 17,
                "accent_color": ACCENT,
                "components": [
                    {"type": 10, "content": "## %s %s" % (plugin, version)},
                    {"type": 10, "content": mini_changelog(body)},
                    {"type": 10,
                     "content": "-# […for full changelog click here](%s)" % release_url},
                    {"type": 14},
                    {"type": 1, "components": [
                        {"type": 2, "style": 5, "label": "Purchase", "url": purchase_url},
                    ]},
                ],
            }
        ],
    }


def main():
    tag = os.environ.get("TAG", "").strip()
    release_url = os.environ.get("RELEASE_URL", "").strip()
    body = os.environ.get("RELEASE_BODY", "")

    # A release event fills all three in. A manual run fills none, so go and look.
    if not release_url:
        tag, release_url, body = fetch_release(
            env("GITHUB_REPOSITORY"), tag, os.environ.get("GITHUB_TOKEN", ""))
        print("notify: resolved release %s from the GitHub API" % tag, file=sys.stderr)

    if not tag:
        sys.exit("notify: could not determine the release tag")

    payload = build_payload(env("PLUGIN_NAME"), tag, release_url, body,
                            env("PURCHASE_URL"))

    if truthy(os.environ.get("DRY_RUN")):
        print(json.dumps(payload, indent=2, ensure_ascii=False))
        return

    webhook = env("DISCORD_WEBHOOK")
    if not webhook.startswith("https://discord.com/api/webhooks/"):
        sys.exit("notify: DISCORD_WEBHOOK is not a Discord webhook URL")

    # Components V2 is rejected as "Cannot send an empty message" without this parameter.
    url = webhook + ("&" if "?" in webhook else "?") + "with_components=true"
    req = urllib.request.Request(
        url,
        data=json.dumps(payload).encode(),
        headers={"Content-Type": "application/json",
                 "User-Agent": "mpd-release-notify/1.0"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            print("notify: Discord accepted the message (HTTP %d)" % resp.status)
    except urllib.error.HTTPError as e:
        # Never echo the URL - it is the secret.
        sys.exit("notify: Discord rejected the message (HTTP %d): %s"
                 % (e.code, e.read().decode("utf-8", "replace")[:300]))
    except urllib.error.URLError as e:
        sys.exit("notify: could not reach Discord: %s" % e.reason)


if __name__ == "__main__":
    main()
