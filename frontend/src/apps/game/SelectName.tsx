import { useDispatch, useSelector } from 'react-redux'
import { State } from './state/store'
import { Action, ThunkDispatch } from '@reduxjs/toolkit'
import React, { useState } from 'react'
import { actions } from './state/gameSlice'
import { attemptConnection } from '../../core/ws/gameServerSlice'
import { QButton, QInput, QVStack } from '../../core/components'

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
    <QVStack>
      <QInput
        placeholder={'Select name for yourself'}
        value={name}
        onChange={event => setName(event.target.value)}/>
      <QButton onClick={assignName}>OK</QButton>
    </QVStack>
  )
}