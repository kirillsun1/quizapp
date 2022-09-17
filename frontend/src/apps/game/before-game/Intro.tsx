import React, { useState } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { State } from '../state/store'
import { Action, ThunkDispatch } from '@reduxjs/toolkit'
import { actions } from '../state/gameSlice'
import { attemptConnection } from '../../../core/ws/gameServerSlice'
import { QBox, QButton, QCenter, QHeading, QInput, QStack } from '../../../core/components'

export function Intro() {
  const currentName = useSelector((state: State) => state.game.playerName)
  const connecting = useSelector((state: State) => state.gameServer.connecting)
  const dispatch = useDispatch<ThunkDispatch<State, {}, Action>>()
  const [name, setName] = useState('')
  const assignName = async () => {
    await dispatch(attemptConnection(name))
    dispatch(actions.selectName(name))
  }

  if (currentName) {
    return null
  }
  return (
    <QCenter minH={'100vh'}>
      <QBox
        background={'white'}
        padding={16}
        borderRadius={36}
        boxShadow="xl"
      >
        <QBox>
          <QStack width={'xl'} paddingX={2}>
            <QBox marginBottom={6}>
              <QHeading marginBottom={2}>Hey there!</QHeading>
              <QHeading size={'md'}>How can we call you?</QHeading>
            </QBox>

            <QStack maxW={'2xl'} direction={'row'}>
              <QInput
                disabled={connecting}
                placeholder={'Name'}
                value={name}
                maxLength={30}
                size={'lg'}
                onChange={event => setName(event.target.value)}
              />
              <QButton
                size={'lg'}
                isLoading={connecting}
                disabled={!name}
                onClick={assignName}
              >
                Go!
              </QButton>
            </QStack>
          </QStack>
        </QBox>
      </QBox>
    </QCenter>
  )
}