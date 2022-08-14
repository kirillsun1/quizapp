import { useDispatch, useSelector } from 'react-redux'
import { State } from './state/store'
import { Action, ThunkDispatch } from '@reduxjs/toolkit'
import React, { useState } from 'react'
import { actions } from './state/gameSlice'
import { attemptConnection } from '../../core/ws/gameServerSlice'
import { QButton, QHeading, QInput, QStack } from '../../core/components'

export function SelectName() {
  const currentName = useSelector((state: State) => state.game.playerName)
  const dispatch = useDispatch<ThunkDispatch<State, {}, Action>>()
  const [name, setName] = useState('')
  const assignName = () => {
    dispatch(actions.selectName(name))
    dispatch(attemptConnection(name))
  }

  if (currentName) {
    return null
  }
  return (
    <QStack>
      <QHeading marginTop={12} marginBottom={6}>Welcome to Quiz!</QHeading>

      <QStack maxW={'2xl'} direction={'row'}>
        <QInput
          placeholder={'How can we call you?'}
          value={name}
          maxLength={30}
          onChange={event => setName(event.target.value)}
        />
        <QButton onClick={assignName} disabled={!name}>Go!</QButton>
      </QStack>
    </QStack>
  )
}