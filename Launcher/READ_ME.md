# Manual
Vindinium clients can be run in two modes: training and ranked. 

Training mode places the bot againts three training bots, which move randomly. It is used to test the bot. 

Ranked mode is played against other bot clients. These games affect the bots ELO ranking and are the main purpose of the bot. 

After starting a game it can be viewed in the Vindinium homesite. 
The game should open automatically in your browser. If not, search logs/app.log for "Game URL" and open the link. 
This log also contains information about the bots activities.

Running instructions:
On Windows: run run_training.bat to start training game and run_rannked.bat to connect to a ranked game. 
On Linux: run run_training.sh to start training game and run_ranmked.sh to connect to a ranked game. (Note that this is untested and may not work...)

If neither method works, open command line and type 
"java -jar vindiniumclient-1.0.0-SNAPSHOT.jar 3nv9w0z3 COMPETITION advanced mybot.MyBot" for ranked mode or
"java -jar vindiniumclient-1.0.0-SNAPSHOT.jar 3nv9w0z3 TRAINING advanced mybot.MyBot" for training mode. 
(3nv9w0z3 is the key used to identify bot at the server and advanced mybot.MyBot is the class that contains the bot)