import { ForbiddenError, NotFoundError, RemoteError } from '../api/error';
import { Intent, Position, Toaster } from '@blueprintjs/core';

export function createToastActions(toaster) {
  return {
    showToast: (message: string) => {
      toaster.show({
        message,
        intent: Intent.NONE,
      });
    },

    showSuccessToast: (message: string) => {
      toaster.show({
        icon: 'tick',
        message,
        intent: Intent.SUCCESS,
      });
    },

    showAlertToast: (message: string) => {
      toaster.show({
        icon: 'envelope',
        message,
        intent: Intent.WARNING,
        timeout: 0,
      });
    },

    showErrorToast: (error: any) => {
      let message: string;
      if (error instanceof RemoteError) {
        message = 'Internal server error; please try again later.';
      } else if (error instanceof ForbiddenError) {
        message = 'Operation not allowed.';
      } else if (error instanceof NotFoundError) {
        message = 'Resource not found.';
      } else {
        message = error.message;
      }

      toaster.show({
        icon: 'warning-sign',
        message,
        intent: Intent.DANGER,
      });
    },
  };
}

const injectedToaster = Toaster.create({
  position: Position.TOP,
  className: 'toast',
});

export const toastActions = createToastActions(injectedToaster);