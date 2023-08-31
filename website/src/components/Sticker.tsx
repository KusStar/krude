/* eslint-disable @next/next/no-img-element */
import { useDrag } from '@use-gesture/react'
import { animated, useSpring, to } from '@react-spring/web'
import clsx from 'clsx'
import { FC, HtmlHTMLAttributes, useRef } from "react"

export const Draggable: FC<HtmlHTMLAttributes<HTMLDivElement>> = ({ className, ...props }) => {
  const ref = useRef<HTMLDivElement>(null)
  const [animatedProps, api] = useSpring(() => ({ x: 0, y: 0, scale: 1 }))

  useDrag(state => {
    api.start({ x: state.offset[0], y: state.offset[1], scale: state.down ? 1.1 : 1 })
  }, {
    target: ref,
    from: () => [animatedProps.x.get(), animatedProps.y.get()],
  })

  return (
    <animated.div
      className={clsx("cursor-grab touch-none", className)}
      style={{
        touchAction: 'none',
        transform: to([animatedProps.x, animatedProps.y, animatedProps.scale], (x, y, scale) => `translate3d(${x}px,${y}px,0) scale(${scale})`),
      }}
      ref={ref}
      {...props}
    >
      <div
        className="pointer-events-none select-none"
      >
        {props.children}
      </div>
    </animated.div>
  )
}