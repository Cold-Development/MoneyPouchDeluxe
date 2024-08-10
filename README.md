![Static Badge](https://img.shields.io/badge/Version-v1.0-blue?color=799aca)
# ColdPouch

![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-blue.svg)<br>
![Minecraft](https://img.shields.io/badge/Minecraft-1.21-green.svg)

**ColdPouch** is a Minecraft plugin that adds customizable pouches to your server. Players can open these pouches to receive rewards, and each pouch can be configured with different textures, names, economies, and more.<br>
- Since the good old plugin **MoneyPouch** was not updated since 2021, here you have it! A newer and updated version that will work for 1.13 up to 1.21 versions. Read the features section to see the updates.
- This is a continuation of the original MoneyPouch plugin by LMBishop which can be found [here](https://github.com/LMBishop/MoneyPouch).

## 📦 Features

- **Customizable Pouches**: Create and configure unique pouches, including using custom player head textures.
- **Economy Integration**: Support for Vault, LemonMobCoins, and other customizable economy systems.
- **Configurable Messages**: Customize the messages displayed to players during pouch interactions.
- **Permission System**: Control access to different pouches via permissions.
- **Shop System**: Includes an in-game shop where players can purchase pouches using various currencies.

## 🛠️ Installation

1. **Download the plugin**: Download the latest version of the ColdPouch plugin from [SpigotMC](https://www.spigotmc.org/resources/authors/padrecxsh.2013368/) page.
2. **Install the plugin**: Copy the `.jar` file into your server's `plugins` directory.
3. **Configure the plugin**: Run the server for the first time to generate the configuration files, then stop it. Edit the `config.yml` file to customize the plugin to your liking.
4. **Start the server**: Start the server again to load the plugin with your custom settings.

## ⚙️ Configuration

### Example `config.yml`

```yaml
# Check out the wiki at https://github.com/padrewin or https://github.com/Cold-Development

pouches:
  tier:
    tier1:
      name: "&8➥ &9&lColdPouches &9✦&7✦✦✦"
      item: "PLAYER_HEAD"
      texture-url: "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzg2MjJhM2Q1M2NjYzc4NDM2YzBmNjYwMDRjYjRiNzcyOWM0NjZlYTEwMDY1ZTgzOWEwNmI2Mjg4YmZkYTk4NiJ9fX0="
      pricerange:
        from: 5000
        to: 15000
      options:
        economytype: "VAULT"
        permission-required: false
      lore:
        - ""
        - "&7&oWhat do you think is in here?"
        - ""
        - "       &8» &f&l5.000&a&l$ &8- &f&l15.000&a&l$ &8«"
        - ""

    ENDtier1:
      name: "&8➥ &9&lColdPouches &9✦&7✦✦✦"
      item: "PLAYER_HEAD"
      texture-url: "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzAxZjY4NjM3OTI2YTg2MTRjNjMzNWIyOWYzM2U3M2Y2NDkyMTNlZmFkZTAzMTJmNTk3YzUyZDc2YzQwZjlkMiJ9fX0="
      pricerange:
        from: 10000
        to: 25000
      options:
        economytype: "VAULT"
        permission-required: false      # requires permission "coldpouches.pouches.ENDtier1"
      lore:
        - ""
        - "&7&oWhat do you think is in here?"
        - ""
        - "       &8» &f&l10.000&a&l$ &8- &f&l25.000&a&l$ &8«"
        - ""
    
    ENDtier2:
      name: "&8➥ &9&lColdPouches &9✦✦&7✦✦"
      item: "ENDER_CHEST"
      pricerange:
        from: 25000
        to: 45000
      options:
        economytype: "VAULT"
        permission-required: false      # requires permission "coldpouches.pouches.ENDtier2"
      lore:
        - ""
        - "&7&oWhat do you think is in here?"
        - ""
        - "       &8» &f&l25.000&a&l$ &8- &f&l45.000&a&l$ &8«"
        - ""
    
    ENDtier3:
      name: "&8➥ &9&lColdPouches &9✦✦✦&7✦"
      item: "ENDER_CHEST"
      pricerange:
        from: 40000
        to: 75000
      options:
        economytype: "VAULT"
        permission-required: false      # requires permission "coldpouches.pouches.ENDtier3"
      lore:
        - ""
        - "&7&oWhat do you think is in here?"
        - ""
        - "       &8» &f&l40.000&a&l$ &8- &f&l75.000&a&l$ &8«"
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
    obfuscate-colour: "&9"
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

# Messages here
messages:
  full-inv: "&8「&9ColdPouches&8」&7» &f%player%'s inventory is full."
  give-item: "&8「&9ColdPouches&8」&7» &fYou have given &9%player%&f %item%&f."
  receive-item: "&8「&9ColdPouches&8」&7» &fYou have received &9%item%&f."
  prize-message: "&8「&9ColdPouches&8」&7» &fYou have received &9%prefix%%prize%%suffix%&f!"
  already-opening: "&8「&9ColdPouches&8」&7» &fPlease wait until you open the first chest!"
  invalid-pouch: "&8「&9ColdPouches&8」&7» &fThis chest no longer exists! &7(contact an administrator)"
  inventory-full: "&8「&9ColdPouches&8」&7» &fYour inventory is full!"
  reward-error: "&8「&9ColdPouches&8」&7» &fThe reward %prefix%%prize%%suffix% &fhas failed."
  purchase-success: "&8「&9ColdPouches&8」&7» &fYou have purchased %item%&f for &9%prefix%%price%%suffix%&f."
  purchase-fail: "&8「&9ColdPouches&8」&7» &9You do not have &9%prefix%%price%%suffix%&f."
  purchase-error: "&8「&9ColdPouches&8」&7» &9Could not complete transaction for %item%&9."
  shop-disabled: "&8「&9ColdPouches&8」&7» &fThe shop is disabled."
  no-permission: "&8「&9ColdPouches&8」&7» &fYou do not have permission to open this chest!"
  reloaded: "&8「&9ColdPouches&8」&7» ColdPouch has been reloaded."
```

# 💻 Commands
- **/coldpouch reload** - Reloads the plugin configuration.
- **/coldpouchshop** - Opens the pouch shop.

# ⚠️ Permissions
- **coldpouches.admin** - Permission to manage the plugin and use admin commands.
- **coldpouches.pouches.<id>** - Permission required to open a specific type of pouch.

<br>![image](https://github.com/user-attachments/assets/d90280ff-2742-4345-b504-811ce1f26fa0)
![image](https://github.com/user-attachments/assets/7f9f283d-7c0a-44a1-8094-1843b4d179dd)
![image](https://github.com/user-attachments/assets/3df02627-6c76-4d80-8d91-e879937f4ddd)
![opening](https://github.com/user-attachments/assets/a7889779-bceb-42c8-b573-8d05e9d49070)
