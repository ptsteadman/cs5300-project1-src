# cs5300-project1

Class List:
  
  -   SessionID: `int, int`  (has .serialize(), .unserialize())
  -   Cookie: `SessionID, int, int, int` (has .serialize(), .unserialize())
  -   SessionState: `int, int, string, long` (has .serialize(), .unserialize())
  -   View: `Map<SessionID, List<Attribute>>` .exchangeWith()s (has .serialize(), .unserialize())
  -   RPC Client: methods listed in assignment
  -   SessionStateServer: `Map<SessionID, SessionState>` (basically assignment 1a)
