import React from 'react'
import { GameServerStatus } from './GameServerStatus'
import { Intro } from './before-game/Intro'
import { ModeratorMode } from './moderator/ModeratorMode'
import { PlayerMode } from './player/PlayerMode'

type Props = { mode: 'player' | 'moderator' }

export const GameScreen = ({mode}: Props) => (
  <GameServerStatus>
    <Intro/>
    {mode === 'player' ? <PlayerMode/> : <ModeratorMode/>}
  </GameServerStatus>
)
