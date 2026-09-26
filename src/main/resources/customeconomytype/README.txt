Custom economies
================

Every file in this folder is an economy a pouch can pay out in. XP is the only built-in one.

  vault.yml                 money through any economy plugin (eco give)    -> economytype: "VAULT"
  playerpoints.yml          PlayerPoints (points give)                     -> economytype: "PlayerPoints"
  examplecustomeconomy.yml  diamonds, as an example                        -> economytype: "examplecustomeconomy"

To add one, copy a file, rename it (the name is the id), and change transaction-prize-command.
Run /mpa reload afterwards.

If this folder or a file is deleted, the pouches using it are skipped with a warning in the console.

See: https://github.com/Cold-Development/MoneyPouchDeluxe/wiki/Custom-Economy-Types
