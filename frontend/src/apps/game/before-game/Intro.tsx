import { useDispatch, useSelector } from 'react-redux'
import { State } from '../state/store'
import { Action, ThunkDispatch } from '@reduxjs/toolkit'
import React, { useState } from 'react'
import { actions } from '../state/gameSlice'
import { attemptConnection } from '../../../core/ws/gameServerSlice'
import { QBox, QButton, QCenter, QHeading, QInput, QStack } from '../../../core/components'

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
      <QBox background={'white'} padding={16} borderRadius={24}>
        <QStack width={'xl'} paddingX={2}>
          <QBox marginBottom={6}>
            <QHeading marginBottom={2}>Hey there!</QHeading>
            <QHeading size={'md'}>Time for a quiz! But before we start...</QHeading>
          </QBox>


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
      </QBox>
    </QCenter>
  )
}