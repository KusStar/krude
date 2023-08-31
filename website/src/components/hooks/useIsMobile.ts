import { useEffect, useState } from "react";
import { isMobile as checkIsMobile } from "is-mobile";

export const useIsMobile = (defaultVal = false) => {
  const [isMobile, setIsMobile] = useState(defaultVal);

  const check = () => {
    const mobile = checkIsMobile()
    setIsMobile(mobile);
  }

  useEffect(() => {
    window.addEventListener("resize", check)
    check()

    return () => {
      window.removeEventListener("resize", check)
    }
  }, []);

  return isMobile;
}