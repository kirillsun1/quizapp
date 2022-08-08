import { QButton, QButtonGroup, QInput, QVStack } from '../../core/components'
import React, { useState } from 'react'
import { actions } from './state/gameSlice'
import { useDispatch, useSelector } from 'react-redux'
import { Action, ThunkDispatch } from '@reduxjs/toolkit'
import { State } from './state/store'

export function BeforeGameChoices() {
  const dispatch = useDispatch<ThunkDispatch<State, {}, Action>>()
  const [joinRoomCode, setJoinRoomCode] = useState('')
  const joinQuiz = () => {
    dispatch(actions.joinRoom(joinRoomCode))
  }
  const hostQuiz = () => {
    dispatch(actions.createRoom())
  }

  const mode = useSelector((state: State) => state.game.mode)
  const playerName = useSelector((state: State) => state.game.playerName)
  if (!playerName) {
    return null
  }
  if (mode) {
    return null
  }

  return (
    <QButtonGroup>
      <QVStack>
        <QInput
          placeholder={'Room code'}
          value={joinRoomCode}
          onChange={event => setJoinRoomCode(event.target.value)}/>
        <QButton onClick={joinQuiz}>
          Join Quiz
        </QButton>
      </QVStack>

      <QButton onClick={hostQuiz}>
        Host Quiz
      </QButton>
    </QButtonGroup>
  )
}