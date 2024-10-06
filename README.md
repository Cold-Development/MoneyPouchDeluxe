![image](https://imgur.com/FRoQbVI.png)<br>
![Static Badge](https://img.shields.io/badge/Version-v1.3.2-blue?color=799aca)
### Supported versions:
![Minecraft](https://img.shields.io/badge/Minecraft-1.20-blue.svg)<br>
![Minecraft](https://img.shields.io/badge/Minecraft-1.21-green.svg)
# 💰 MoneyPouchDeluxe

**MoneyPouchDeluxe** is a Minecraft plugin that adds customizable pouches to your server. Players can open these pouches to receive rewards, and each pouch can be configured with different textures, names, economies, and more.<br>
- This is a continuation of the original MoneyPouch plugin by LMBishop which can be found [here](https://github.com/LMBishop/MoneyPouch).

## 📦 Features

- **Customizable Pouches**: Create and configure unique pouches, including using custom player head textures.
- **Economy Integration**: Support for Vault, LemonMobCoins, and other customizable economy systems.
- **Configurable Messages**: Customize the messages displayed to players during pouch interactions.
- **Permission System**: Control access to different pouches via permissions.
- **Shop System**: Includes an in-game shop where players can purchase pouches using various currencies.

## 🛠️ Installation

1. **Download the plugin**: Download the latest version of the MoneyPouchDeluxe plugin from [SpigotMC](https://www.spigotmc.org/resources/moneypouchdeluxe.118795/) page.
2. **Install the plugin**: Copy the `.jar` file into your server's `plugins` directory.
3. **Configure the plugin**: Run the server for the first time to generate the configuration files, then stop it. Edit the `config.yml` file to customize the plugin to your liking.
4. **Start the server**: Start the server again to load the plugin with your custom settings.

## ⚙️ Configuration

### Example `config.yml`

```yaml
#########################################################################################################
#    Developer; padrewin || Cold Development                                                            #
#    GitHub; https://github.com/padrewin || https://github.com/Cold-Development                         #
#    Links: https://colddev.dev || https://discord.colddev.dev                                          #
#    Textures: https://minecraft-heads.com/custom-heads                                                 #
#########################################################################################################

# Holograms for pouches
holograms:
  enabled: false

# If you don't want to use custom textures such as "PLAYER_HEAD" just replace it with "CHEST" or "ENDER_CHEST" and leave "texture-url:" empty. You have an example below somewhere.
# You can use placeholders in the pouch's lore. %pricerange_from% and %pricerange_to%
# economytypes can be: VAULT (money), XP, PlayerPoints, LemonMobCoins, owncustomname (name of defined custom pouch)

pouches:
  tier:
    moneypouch:
      name: "&8➥ &6&lMoney Pouch &6✦&7✦✦✦"
      item: "CHEST"
      texture-url: ""
      pricerange:
        from: 5000
        to: 15000
      options:
        economytype: "VAULT"
        permission-required: false
      lore:
        - ""
        - "&7&oHow much money do you think is in here?"
        - ""
        - "       &8» &f&l%pricerange_from%&a&l$ &8- &f&l%pricerange_to%&a&l$ &8«"
        - ""

    pointspouch:
      name: "&8➥ &6&lPoints Pouch &6✦&7✦✦✦"
      item: "PLAYER_HEAD"
      texture-url: "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTVmZDY3ZDU2ZmZjNTNmYjM2MGExNzg3OWQ5YjUzMzhkNzMzMmQ4ZjEyOTQ5MWE1ZTE3ZThkNmU4YWVhNmMzYSJ9fX0="
      pricerange:
        from: 5
        to: 15
      options:
        economytype: "PlayerPoints" # Add PlayerPoints as a valid economy type
        permission-required: false
      lore:
        - ""
        - "&7&oHow much points do you think is in here?"
        - ""
        - "       &8» &f&l5&a&l Points &8- &f&l15&a&l Points &8«"
        - ""

    xppouch:
      name: "&8➥ &6&lXP Pouch &6✦&7✦✦✦"
      item: "ENDER_CHEST"
      texture-url: ""
      pricerange:
        from: 10
        to: 150
      options:
        economytype: "XP"
        permission-required: false      # requires permission "moneypouch.pouches.xppouch"
      lore:
        - ""
        - "&7&oHow much XP do you think is in here?"
        - ""
        - "       &8» &f&l10&a&lXP &8- &f&l150&a&lXP &8«"
        - ""

  # Sound settings (!!!!!!! MUST change for pre-1.9 !!!!!!!)
  sound:
    enabled: true
    opensound: "BLOCK_CHEST_OPEN"       # (CHEST_OPEN  pre-1.9)
    revealsound: "BLOCK_ANVIL_LAND"     # (ANVIL_LAND  pre-1.9)
    endsound: "ENTITY_GENERIC_EXPLODE"  # (EXPLODE  pre-1.9)

  # Title settings (will not work before 1.8, timings will not work before 1.10)
  title:
    speed-in-tick: 10
    subtitle: "&7&oOpening..."
    obfuscate-colour: "&6"
    reveal-colour: "&f&l"
    prefix-colour: "&a&l"
    suffix-colour: "&a"
    obfuscate-digit-char: "#"
    obfuscate-format-char: "|"
    format:               # (adds commas e.g   $1,924,281)
      enabled: false
      reveal-comma: true  # the commas will already be revealed when opening

# Rather than showing each digit left-to-right, reveal it right-to-left
reverse-pouch-reveal: true

error-handling:
  # It is recommended you set the following to 'true' in a production environment
  # The plugin will log should a transaction fail for any reason, allowing you to investigate
  # and manually reward the player yourself
  # The player will be alerted and asked to tell an admin should this event occur regardless if this is disabled
  # You can change this message ('reward-error') at the bottom of the config
  log-failed-transactions: true
  # Refund the pouch to the player (if they are online) in the event a transaction failed - this is default
  # to 'false' as this results in a different prize on the second try, and it is unlikely
  # that the transaction will succeed if it had already failed. It is recommended
  # to keep this 'false' and manually investigate when errors occur.
  refund-pouch: false
  # Prevent opening pouches which have an invalid economy type assigned to them.
  # The message 'invalid-pouch' will be sent to the player.
  prevent-opening-invalid-pouches: true

# /mpshop
shop:
  enabled: false
  ui-title: "Pouch Shop"
  purchasable-items:
    xp-1:
      price: 4000
      currency: "VAULT"
    vault-1:
      price: 5500
      currency: "VAULT"
  append-to-lore:
    - "&8==========================="
    - "&aPrice: &e%prefix%%price%%suffix%"
    - "&aClick to purchase"

# Economy prefixes and suffixes
economy:
  xp:
    prefix: ""
    suffix: " XP"
  vault:
    prefix: "$"
    suffix: ""
  lemonmobcoins:
    prefix: ""
    suffix: " Mob Coins"
  playerpoints:
    prefix: ""
    suffix: " Points"

# Messages here
messages:
  full-inv: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» &6%player%'s &finventory is &cfull&f. The pouch was dropped near the player."
  player-full-inv: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» &fYour inventory is &cfull&f. A pouch was dropped near you. Make sure to pick it up."
  give-item: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» &fYou have given &6%player%&f %item%&f."
  receive-item: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» &fYou have received &6%item%&f."
  prize-message: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» &fYou have received &6%prefix%%prize%%suffix%&f!"
  already-opening: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» &fPlease wait until you open the first chest!"
  invalid-pouch: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» &fThis chest no longer exists! &7(contact an administrator)"
  inventory-full: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» &fYour inventory is full!"
  reward-error: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» &fThe reward %prefix%%prize%%suffix% &fhas failed."
  purchase-success: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» &fYou have purchased %item%&f for &6%prefix%%price%%suffix%&f."
  purchase-fail: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» &6You do not have &6%prefix%%price%%suffix%&f."
  purchase-error: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» &6Could not complete transaction for %item%&6."
  shop-disabled: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» &fThe shop is disabled."
  no-permission: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» &fYou do not have permission to open this chest!"
  reloaded: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» MoneyPouchDeluxe has been reloaded."
  update_notification: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» &cA new version &4%latest_version% &cwas found &4(your version: %current_version%)&c. &cPlease update: &n%update_link%"
  kill-holo: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» Pouch hologram removed."
  holograms_enabled: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» Holograms are now enabled."
  holograms_disabled: "&8「&x&F&F&6&7&0&0Mo&x&F&E&7&6&0&0ne&x&F&C&8&B&0&0yP&x&F&B&6&D&0&0ou&x&F&A&B&0&0&0ch&x&F&6&C&2&0&0De&x&F&7&D&4&0&0lu&x&F&6&E&6&0&0xe&8」&7» Holograms are now disabled."
```

# 💻 Commands
![image](https://github.com/user-attachments/assets/1e5c4e68-0b45-450d-a777-bdd84bbdc57c)

# ⚠️ Permissions
- **moneypouch.admin** - Permission to manage the plugin and use admin commands.
- **moneypouch.pouches.<id>** - Permission required to open a specific type of pouch.

<br>![image](https://github.com/user-attachments/assets/280b898b-df87-476b-b208-96505929eea1)
![image](https://github.com/user-attachments/assets/efca13c8-bb79-4087-a983-8492c443bb3a)<br>
![image](https://github.com/user-attachments/assets/1e5c4e68-0b45-450d-a777-bdd84bbdc57c)<br>
![opening](https://github.com/user-attachments/assets/a7889779-bceb-42c8-b573-8d05e9d49070)

#### Downloads:
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/Cold-Development/MoneyPouchDeluxe/total?color=green)
