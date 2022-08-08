import React from 'react'
import { QContainer, QText } from '../../core/components'
import { useSelector } from 'react-redux'
import type { State } from './state/store'
import { GameServerStatus } from './GameServerStatus'
import { SelectName } from './SelectName'
import { BeforeGameChoices } from './BeforeGameChoices'
import { PlayerWelcome } from './PlayerWelcome'
import { ModeratorScreen } from './ModeratorScreen'
import { PlayerScreen } from './PlayerScreen'

export function GameScreen() {
  const stateSnapshot = useSelector((state: State) => JSON.stringify(state))

  return (
    <QContainer maxW="6xl">
      <QText fontSize={'xs'}>
        {stateSnapshot}
      </QText>
      <SelectName/>
      <GameServerStatus>
        <PlayerWelcome/>
        <BeforeGameChoices/>
        <ModeratorScreen/>
        <PlayerScreen/>
      </GameServerStatus>
    </QContainer>
  )
}
