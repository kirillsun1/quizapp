import React from 'react'
import { QBox } from '../chakra'

export function QBackground({children}: { children: React.ReactNode }) {
  return (
    <QBox bgGradient={'linear(11deg, #74EBD5, #9FACE6)'} minH={'100vh'}>
      {children}
    </QBox>
  )
}