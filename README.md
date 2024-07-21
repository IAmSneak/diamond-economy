Basic economy for the fabric modloader using diamonds as currency. Uses the "/diamonds" command along with the following subcommands

- balance [Optional: player] - tells user how much money the player has

- deposit - takes currency in your hand and adds it to your account
  - deposit all [Optional: int] - takes all currency items from your inventory and adds it to your account. [int] specifies a maximum deposit amount

- send [player] [int] - takes $[int] from your account and adds them to [player]'s account

- top [Optional: int] - tells user current rank along with who has the most money, int for page #

- withdraw [int] - takes $[int] from your account and puts it into your inventory in as many high value currency items as possible
 

Operator only subcommands:

- modify [players] [int] - modifies [players] money by $[int]

- take [players] [int] - takes $[int] from [players]


Placeholders:
- %diamondeconomy:rank_from_player%
- %diamondeconomy:rank_from_string_uuid%
- %diamondeconomy:balance_from_player%
- %diamondeconomy:balance_from_name%
- %diamondeconomy:balance_from_rank%
- %diamondeconomy:balance_from_string_uuid%


Downloads:
- https://www.curseforge.com/minecraft/mc-mods/diamond-economy
- https://modrinth.com/mod/diamond-economy
