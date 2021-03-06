import { FileInput } from '@blueprintjs/core';
import * as React from 'react';

import { FormInputProps } from '../props';
import { FormTableInput } from '../FormTableInput/FormTableInput';

export interface FormTableFileInputProps extends FormInputProps {
  placeholder?: string;
}

const handleChange = onChange => e => {
  e.preventDefault();
  const { target } = e;
  return onChange(target.files.length ? target.files[0] : undefined);
};

export const FormTableFileInput = (props: FormTableFileInputProps) => {
  const { value, onChange, onBlur, ...inputProps } = props.input;
  const placeholder = props.placeholder || 'Choose file...';
  return (
    <FormTableInput {...props}>
      <FileInput
        inputProps={{ onChange: handleChange(onChange), ...inputProps }}
        text={value ? value.name : placeholder}
      />
    </FormTableInput>
  );
};
