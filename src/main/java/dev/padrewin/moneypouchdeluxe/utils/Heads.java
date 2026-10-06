package dev.padrewin.moneypouchdeluxe.utils;

import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom player-head textures, e.g. from https://minecraft-heads.com/custom-heads. Accepts any of
 * the formats those sites give:
 * <ul>
 *     <li>the Base64 "Value" ({@code eyJ0ZXh0dXJlcyI6...})</li>
 *     <li>the texture URL ({@code http://textures.minecraft.net/texture/<hash>})</li>
 *     <li>just the texture hash ({@code 95fd67d56ffc53fb...})</li>
 * </ul>
 * Servers with Bukkit's PlayerProfile API (1.18.1+, including 26.x) use it; older ones set the
 * Mojang GameProfile through reflection. Which one is picked by checking what the server has, not by
 * parsing its version, so new version numbering can't break it.
 */
public final class Heads {

    private static final Pattern URL_IN_JSON = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern TEXTURE_HASH = Pattern.compile("[0-9a-fA-F]{32,64}");
    // http, like the URLs inside Mojang's own texture values, so every format gives the same URL
    private static final String TEXTURE_SERVER = "http://textures.minecraft.net/texture/";

    private static final boolean PROFILE_API = hasProfileApi();

    private Heads() {
    }

    /**
     * Puts the texture on the head's meta, keeping everything else on it (name, lore, flags, ...).
     *
     * @return false if the texture isn't in a format we understand (the meta is left untouched)
     */
    public static boolean applyTexture(SkullMeta meta, String texture) {
        URL url = toTextureUrl(texture);
        if (url == null) {
            return false;
        }
        // The same texture always gets the same profile id, so pouches created before and after a
        // reload are identical and keep stacking with each other
        UUID id = UUID.nameUUIDFromBytes(("MoneyPouchDeluxe:" + url).getBytes(StandardCharsets.UTF_8));
        return PROFILE_API ? applyWithProfileApi(meta, id, url) : applyWithReflection(meta, id, url);
    }

    /**
     * @return the texture URL from a Base64 value, a URL or a bare hash, or null if it's none of those
     */
    static URL toTextureUrl(String texture) {
        if (texture == null || texture.isBlank()) {
            return null;
        }
        String value = texture.trim();

        if (TEXTURE_HASH.matcher(value).matches()) {
            value = TEXTURE_SERVER + value.toLowerCase();
        } else if (!value.startsWith("http://") && !value.startsWith("https://")) {
            try {
                value = new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                return null;
            }
            Matcher matcher = URL_IN_JSON.matcher(value);
            if (!matcher.find()) {
                return null;
            }
            value = matcher.group(1).replace("\\/", "/");
        }

        try {
            return URI.create(value).toURL();
        } catch (IllegalArgumentException | java.net.MalformedURLException e) {
            return null;
        }
    }

    private static boolean hasProfileApi() {
        try {
            Class<?> profileClass = Class.forName("org.bukkit.profile.PlayerProfile");
            Bukkit.class.getMethod("createPlayerProfile", UUID.class);
            SkullMeta.class.getMethod("setOwnerProfile", profileClass);
            return true;
        } catch (ReflectiveOperationException | LinkageError e) {
            return false;
        }
    }

    private static boolean applyWithProfileApi(SkullMeta meta, UUID id, URL url) {
        return ProfileApi.apply(meta, id, url);
    }

    /**
     * Kept in its own class so the org.bukkit.profile classes are only loaded on servers that have them.
     */
    private static final class ProfileApi {

        static boolean apply(SkullMeta meta, UUID id, URL url) {
            PlayerProfile profile = Bukkit.createPlayerProfile(id);
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(url);
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
            return true;
        }
    }

    /**
     * Pre-1.18.1: builds a com.mojang.authlib GameProfile carrying the textures property and sets it
     * on CraftMetaSkull. All through reflection, so the plugin doesn't need authlib to compile.
     */
    @SuppressWarnings("unchecked")
    private static boolean applyWithReflection(SkullMeta meta, UUID id, URL url) {
        String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
        String base64 = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        try {
            Class<?> gameProfileClass = Class.forName("com.mojang.authlib.GameProfile");
            Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");

            Constructor<?> gameProfileConstructor = gameProfileClass.getConstructor(UUID.class, String.class);
            Object profile = gameProfileConstructor.newInstance(id, null);
            Object property = propertyClass.getConstructor(String.class, String.class).newInstance("textures", base64);
            Object properties = gameProfileClass.getMethod("getProperties").invoke(profile);
            ((Multimap<String, Object>) properties).put("textures", property);

            // setProfile also fills the serialized profile on the versions that have it; otherwise set the field
            try {
                Method setProfile = meta.getClass().getDeclaredMethod("setProfile", gameProfileClass);
                setProfile.setAccessible(true);
                setProfile.invoke(meta, profile);
            } catch (NoSuchMethodException e) {
                Field profileField = meta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(meta, profile);
            }
            return true;
        } catch (ReflectiveOperationException | RuntimeException e) {
            Bukkit.getLogger().warning("[MoneyPouchDeluxe] Could not apply a custom head texture on this server version: " + e);
            return false;
        }
    }

}
