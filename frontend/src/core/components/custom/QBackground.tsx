import React from 'react'
import { QBox } from '../chakra'

export function QBackground({children}: { children: React.ReactNode }) {
  return (
    <QBox bgGradient={'linear(19deg, #08AEEA, #2AF598)'}>
      {children}
    </QBox>
  )
}