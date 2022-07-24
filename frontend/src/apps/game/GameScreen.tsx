import React, { useState } from 'react'
import { QButton, QButtonGroup, QContainer, QInput, QText, QVStack } from '../../core/components'
import { useDispatch, useSelector } from 'react-redux'
import { assignPlayerName } from './state/playerNameSlice'
import type { State } from './state/store'

export function GameScreen() {
  return (
    <QContainer>
      <SelectName/>
      <Welcome/>
    </QContainer>
  )
}

function SelectName() {
  const currentName = useSelector((state: State) => state.playerName.value)
  const dispatch = useDispatch()
  const assignName = () => dispatch(assignPlayerName(name))

  const [name, setName] = useState('')

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

  if (!currentName) {
    return null
  }
  return (
    <QVStack>
      <QText>Hi, {currentName}!</QText>
      <QButtonGroup>
        <QButton>
          Join Quiz
        </QButton>
        <QButton>
          Host Quiz
        </QButton>
      </QButtonGroup>
    </QVStack>
  )
}