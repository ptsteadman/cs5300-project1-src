# cs5300-project1

Class List:

  -   SessionID: `int, int`  (has .serialize(), .unserialize())
  -   Cookie: `SessionID, int, int, int` (has .serialize(), .unserialize())
  -   SessionState: `int, int, string, long` (has .serialize(), .unserialize())
  -   View: `Map<SessionID, List<Attribute>>` .exchangeWith()s (has .serialize(), .unserialize())
  -   RPC Client: methods listed in assignment
  -   SessionStateServer: `Map<SessionID, SessionState>` (basically assignment 1a)

## How to make git play nice with eclipse

1. Clone this repo, call this `<git_repo>`
2. Create a new AWS project in eclipse
3. Go into `~/workspace/<your_aws_project>/src`
4. Make softlinks to __all__ the source files in `<git_repo>/src/`
5. In the future, if you pull new source files from git, repeat steps 3-4
6. If you want to create a new file, create it in `<git_repo>`, and softlink to your eclipse project
7. Profit
