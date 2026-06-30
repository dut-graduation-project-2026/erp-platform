"use client";

import { AxiosError } from "axios";
import { toast } from "sonner";

import type { HttpAxiosResponse } from "@/types/auth";
import { useRef, useCallback } from "react";

const getAxiosMessage = (error: AxiosError<HttpAxiosResponse>) => {
  const message = error.response?.data?.message;
  return message || error.message;
};

export const getMessage = (message: unknown, fallback = "Request failed") => {
  if (message instanceof AxiosError) {
    return getAxiosMessage(message) || fallback;
  }

  if (message instanceof Error && message.message) {
    return message.message;
  }

  if (typeof message === "string" && message.length > 0) {
    return message;
  }

  return fallback;
};

export const useToast = () => {
  const currentToastId = useRef<string | number | null>(null);

  const dismissCurrent = () => {
    if (currentToastId.current) {
      toast.dismiss(currentToastId.current);
      currentToastId.current = null;
    }
  };

  const toastSuccess = useCallback((message: string) => {
    dismissCurrent();
    currentToastId.current = toast.success(message);
  }, []);

  const toastError = useCallback((error: unknown, fallback = "Request failed") => {
    dismissCurrent();
    const message = getMessage(error, fallback);
    currentToastId.current = toast.error(message);
    return message;
  }, []);

  const toastInfo = useCallback((message: string) => {
    dismissCurrent();
    currentToastId.current = toast.info(message);
  }, []);

  return {
    toastSuccess,
    toastError,
    toastInfo,
  };
};
