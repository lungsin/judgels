import { selectUnit } from '@formatjs/intl-utils';
import * as React from 'react';
import { FormattedRelativeTime } from 'react-intl';

export interface FormattedRelativeProps {
  value: number;
}

export const FormattedRelative = (props: FormattedRelativeProps) => {
  const { value, unit } = selectUnit(props.value);
  return <FormattedRelativeTime value={value} unit={unit} />;
};
