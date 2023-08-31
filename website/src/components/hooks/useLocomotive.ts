import type LocomotiveScroll from "locomotive-scroll";
import { useEffect, useState } from "react";
import { useIsMobile } from "./useIsMobile";

export const useLocomotive = () => {
  const [instance, setInstance] = useState<LocomotiveScroll>()
  const isMobile = useIsMobile(true)

  useEffect(() => {
    if (isMobile) return
    import('locomotive-scroll').then(({ default: LocomotiveScroll }) => {
      const locomotiveScroll = new LocomotiveScroll({
        lenisOptions: {
          smoothTouch: true
        }
      });

      setInstance(locomotiveScroll)
    })
  }, [isMobile])

  return instance
}