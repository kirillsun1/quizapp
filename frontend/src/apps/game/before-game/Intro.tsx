import { useDispatch, useSelector } from 'react-redux'
import { State } from '../state/store'
import { Action, ThunkDispatch } from '@reduxjs/toolkit'
import React, { useState } from 'react'
import { actions } from '../state/gameSlice'
import { attemptConnection } from '../../../core/ws/gameServerSlice'
import { QButton, QCenter, QHeading, QInput, QStack } from '../../../core/components'

export function Intro() {
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
    <QCenter minH={'100vh'}>
      <QStack w={'xl'} px={'2'}>
        <QHeading marginBottom={6}>Hey there!</QHeading>

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
    </QCenter>
  )
}