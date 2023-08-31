import { FC, HtmlHTMLAttributes } from "react"

export const Spacer: FC<HtmlHTMLAttributes<HTMLDivElement>> = ({ ...props }) => {
  return <div {...props} />
}
