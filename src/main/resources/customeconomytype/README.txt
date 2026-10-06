Custom economies
================

Every file in this folder is an economy a pouch can pay out in. XP is the only built-in one.

  vault.yml                 money through Vault (hook: vault)                 -> economytype: "VAULT"
  playerpoints.yml          PlayerPoints (hook: playerpoints)                 -> economytype: "PlayerPoints"
  examplecustomeconomy.yml  diamonds through a command, as an example         -> economytype: "examplecustomeconomy"

How a file pays the player:
  hook: vault          straight through Vault (needs Vault + an economy plugin)
  hook: playerpoints   straight through the PlayerPoints API
  hook: command        (or no hook) runs transaction-prize-command from the console

A hook knows when a transaction fails, so the player gets 'reward-error' instead of the prize
message and the failure is logged. A command can only tell that it doesn't exist. If the hooked
plugin isn't installed, transaction-prize-command is used instead (when the file has one).

To add one, copy a file, rename it (the name is the id), and change hook / transaction-prize-command.
Run /mpa reload afterwards.

If this folder or a file is deleted, the pouches using it are skipped with a warning in the console.
Exception: VAULT and PlayerPoints keep working without their file while Vault / PlayerPoints is
installed, with the prefix/suffix from economy.vault / economy.playerpoints in config.yml.

See: https://github.com/Cold-Development/MoneyPouchDeluxe/wiki/Custom-Economy-Types
