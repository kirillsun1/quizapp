import React from 'react'
import { QContainer } from '../../core/components'
import { GameServerStatus } from './GameServerStatus'
import { SelectName } from './SelectName'
import { BeforeGameChoices } from './BeforeGameChoices'
import { PlayerWelcome } from './PlayerWelcome'
import { ModeratorScreen } from './ModeratorScreen'
import { PlayerScreen } from './PlayerScreen'

export function GameScreen() {
  return (
    <QContainer maxW="5xl">
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
