#!/usr/bin/env bash

PROJECT_ROOT="/home/ubuntu/app"
JAR_FILE="$PROJECT_ROOT/fridge-rescue-server.jar"

DEPLOY_LOG="$PROJECT_ROOT/deploy.log"

TIME_NOW=$(date +%c)

# 현재 구동 중인 Application PID 확인
CURRENT_PID=$(pgrep -f $JAR_FILE)

# 프로세스가 켜져 있는 경우 종료
if [ -z $CURRENT_PID ]; then
    echo "$TIME_NOW > 현재 실행중인 Application 없음" >> $DEPLOY_LOG
else
  echo "$TIME_NOW > 실행중인 $CURRENT_PID Application 종료" >> $DEPLOY_LOG
  kill  -15 $CURRENT_PID
fi
