/* eslint-disable @next/next/no-img-element */
import { useLocomotive } from "@/components/hooks/useLocomotive"
import Head from "next/head"
import { Draggable } from "@/components/Sticker"
import { Spacer } from "@/components/Spacer"


const Index = () => {
  useLocomotive()

  const toDownload = () => {
    window.open("https://github.com/KusStar/krude")
  }

  return (
    <>
      <Head>
        <title>Krude</title>
        <link rel="apple-touch-icon" sizes="180x180" href="/apple-touch-icon.png"></link>
        <link rel="icon" type="image/png" sizes="32x32" href="/favicon-32x32.png"></link>
        <link rel="icon" type="image/png" sizes="16x16" href="/favicon-16x16.png"></link>
        <link rel="manifest" href="/site.webmanifest"></link>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=0"></meta>
      </Head>
      <div className="w-full bg-gray-200 dark:bg-neutral-950 text-black dark:text-white">
        <div className="min-h-screen w-full flex justify-center items-center flex-col p-6">
          <div data-scroll data-scroll-speed="0.25">
            <Draggable>
              <img src="/android-chrome-512x512.png" alt="icon"
                className="sm:w-64 sm:h-64 h-48 w-48"
              />
            </Draggable>
          </div>
          <div
            data-scroll data-scroll-speed="0.25"
            className="flex justify-center items-center rounded flex-col text-center text-xl tracking-wide"
          >
            <p className="text-3xl font-bold  tracking-wider">Krude</p>
            <Spacer className="m-4" />
            <div data-scroll data-scroll-speed="0.1">
              <p className="opacity-75">A tiny but effective, elegant app launcher.</p>
            </div>
            <Spacer className="m-4" />
            <button
              data-scroll data-scroll-speed="0.2"
              className="rounded-full bg-green-600 text-white text-xl px-4 py-2 active:bg-green-700 outline-green-500"
              onClick={toDownload}
            >
              Download
            </button>
          </div>
        </div>
        <div className="min-h-screen w-full flex justify-center items-center bg-gray-200 dark:bg-neutral-950 flex-col">
          <Draggable>
            <p
              className="text-5xl font-bold tracking-wider">
              Krude
            </p>
          </Draggable>
          <Spacer className="m-2" data-scroll data-scroll-speed="0.25"></Spacer>
          <div data-scroll data-scroll-speed="0.25" className="flex flex-col justify-center items-center">
            <a
              className="text-3xl hover:underline" href="https://github.com/KusStar" target="_blank">@嚴肅遊戲</a>
            <Spacer className="m-2" />
            <Draggable>
              <img
                className="rounded-lg w-32 h-32"
                src="https://avatars.githubusercontent.com/u/21271495?v=5" alt="avatar" />
            </Draggable>
          </div>
        </div>
      </div>
    </>
  )

}
export default Index