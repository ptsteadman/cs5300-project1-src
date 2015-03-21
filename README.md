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
3. Copy your AWS credientials `AwsCredentials.properties` to `<git_repo>/src/`
4. Go into `~/workspace/<your_aws_project>`
5. Delete the existing src directory, and make a softlink to the repo src
   directory with `ln -s <git_repo>/src src`.
6. In theory, your edits to files in eclipse will be reflected in the
   `<git_repo>`
6. The .gitignore will ignore your AwsCredentials.properties file when you
   commit
7. Profit
