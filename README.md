# 刷題趣！

刷題趣！ is a real-time quiz answering game. Players are faced multiple-choice questions and the one won the most score wins the game.

## Features

- Singleplayer and Multiplayer
- Leaderboard system
- Quiz making tools
- A fun learning experience :)

## Run

Get [the latest stable tags](https://github.com/CarrieForle/quiz/tags) from the repo and run the following lines in cmd or powershell.

### Run client

```powershell
cd quiz
.\run-client
```

### Run server

```powershell
cd quiz
.\run-server
```

You can apply 2 optional arguments to constrain min and max number of players. For example to run a server with minimum 2 player and maximum 6.

```powershell
.\run-server 2 6
```

### Docker

The server can be ran as a docker service.

```bash
docker compose up -d
```

#### Footnotes

This is a project for college in Taiwan. We do not accept any contribution **to this repo**.