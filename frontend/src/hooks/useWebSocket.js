// TICKET-ADV115 — useWebSocket(url) with auto-reconnect (exp backoff)

import { useCallback, useEffect, useRef, useState } from "react";

export function useWebSocket(
  url,
  {
    reconnect = true,
    maxRetries = 5,
    baseDelay = 500,
    maxDelay = 30000,
  } = {}
) {

  const [data, setData] = useState(null);
  const [status, setStatus] = useState("connecting");

  const socketRef = useRef(null);
  const retriesRef = useRef(0);
  const reconnectTimerRef = useRef(null);
  const stoppedRef = useRef(false);


  const connect = useCallback(() => {

    if (stoppedRef.current) return;


    const socket = new WebSocket(url);

    socketRef.current = socket;


    socket.onopen = () => {
      setStatus("open");
      retriesRef.current = 0;
    };


    socket.onmessage = (event) => {
      try {
        setData(JSON.parse(event.data));
      } catch {
        setData(event.data);
      }
    };


    socket.onerror = () => {
      setStatus("error");
    };


    socket.onclose = () => {

      if (stoppedRef.current) return;


      setStatus("closed");


      if (
        reconnect &&
        retriesRef.current < maxRetries
      ) {

        const delay = Math.min(
          maxDelay,
          baseDelay * 2 ** retriesRef.current
        );


        retriesRef.current += 1;


        reconnectTimerRef.current = setTimeout(
          connect,
          delay
        );

      }

    };


  }, [
    url,
    reconnect,
    maxRetries,
    baseDelay,
    maxDelay
  ]);



  useEffect(() => {

    stoppedRef.current = false;

    connect();


    return () => {

      stoppedRef.current = true;


      if (reconnectTimerRef.current) {
        clearTimeout(reconnectTimerRef.current);
      }


      if (socketRef.current) {
        socketRef.current.close();
      }

    };


  }, [connect]);



  const send = useCallback((payload) => {

    const socket = socketRef.current;


    if (
      socket &&
      socket.readyState === WebSocket.OPEN
    ) {

      socket.send(
        typeof payload === "string"
          ? payload
          : JSON.stringify(payload)
      );

    }

  }, []);



  return {
    data,
    status,
    send,
  };

}