import React from 'react'
import { GameServerStatus } from './GameServerStatus'
import { Intro } from './before-game/Intro'
import { ModeratorMode } from './moderator/ModeratorMode'
import { PlayerMode } from './player/PlayerMode'
import { QBackground } from '../../core/components'
import { useSelector } from 'react-redux'
import { State } from './state/store'

type Props = { mode: 'player' | 'moderator' }

export function GameScreen({mode}: Props) {
  const playerNameAssigned = useSelector((state: State) => !!state.game.playerName)

  return (
    <GameServerStatus>
      <QBackground>
        {
          !playerNameAssigned
            ? <Intro/>
            : (mode === 'player' ? <PlayerMode/> : <ModeratorMode/>)
        }
      </QBackground>
    </GameServerStatus>
  )
}
