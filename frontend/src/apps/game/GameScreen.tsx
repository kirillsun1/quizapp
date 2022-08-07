import React, { useState } from 'react'
import { QButton, QButtonGroup, QContainer, QInput, QText, QVStack } from '../../core/components'
import { useDispatch, useSelector } from 'react-redux'
import { assignPlayerName } from './state/playerNameSlice'
import { createRoom } from './state/moderatorSlice'
import type { State } from './state/store'
import { GameServerStatus } from './GameServerStatus'
import { attemptConnection } from '../../core/ws/gameServerSlice'
import { Action, ThunkDispatch } from '@reduxjs/toolkit'

export function GameScreen() {
  return (
    <QContainer>
      <SelectName/>
      <GameServerStatus>
        <Welcome/>
        <Moderator/>
      </GameServerStatus>
    </QContainer>
  )
}

function SelectName() {
  const currentName = useSelector((state: State) => state.playerName.value)
  const dispatch = useDispatch<ThunkDispatch<State, {}, Action>>()
  const [name, setName] = useState('')
  const assignName = () => {
    dispatch(assignPlayerName(name))
    dispatch(attemptConnection(name))
  }

  if (currentName) {
    return null
  }
  return (
    <QVStack>
      <QInput
        placeholder={'Select name for yourself'}
        value={name}
        onChange={event => setName(event.target.value)}/>
      <QButton onClick={assignName}>OK</QButton>
    </QVStack>
  )
}

function Welcome() {
  const currentName = useSelector((state: State) => state.playerName.value)
  const roomCode = useSelector((state: State) => state.moderator.roomCode)
  const dispatch = useDispatch<ThunkDispatch<State, {}, Action>>()
  const hostQuiz = () => {
    dispatch(createRoom())
  }

  if (!currentName) {
    return null
  }
  if (roomCode) {
    return null
  }
  return (
    <QVStack>
      <QText>Hi, {currentName}!</QText>
      <QButtonGroup>
        <QButton>
          Join Quiz
        </QButton>
        <QButton onClick={hostQuiz}>
          Host Quiz
        </QButton>
      </QButtonGroup>
    </QVStack>
  )
}

function Moderator() {
  const roomCode = useSelector((state: State) => state.moderator.roomCode)
  if (!roomCode) {
    return null
  }

  return (
    <QVStack>
      <QText>You will moderate room {roomCode}!</QText>
      <QText>Invite players!</QText>
    </QVStack>
  )
}