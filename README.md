# 刷題趣！

刷題趣！ is a real-time quiz answering game. Players are faced multiple-choice questions and the one won the most score wins the game.

## Features

- Singleplayer and Multiplayer
- Leaderboard system
- Quiz making tools
- A fun learning experience :)

## Run

Install JDK 17+ (21 Recommended) and get [the latest stable tags](https://github.com/CarrieForle/quiz/tags) from the repo and run the following lines in cmd or powershell.

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

You can apply 5 optional *positional* arguments as follows:
- minimum player to start (default: 2)
- maximum player to start (default: 4)
- bound port (default: 12345)
- time until game starts in second after player count reaches the minimum (default: 15)
- time until game starts in second after player count reaches the maximum (default: 10)

### Docker

The server can be ran as a docker service.

```bash
docker compose up -d
```

#### Footnotes

This is a project for college in Taiwan. We do not accept any contribution **to this repo**.