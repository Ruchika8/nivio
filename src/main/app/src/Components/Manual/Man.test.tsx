import React from 'react';
import { render } from '@testing-library/react';
import Man from './Man';
import { MemoryRouter } from 'react-router-dom';

it('should render manual component', () => {
  const { getByText } = render(
    <MemoryRouter>
      <Man />
    </MemoryRouter>
  );
  expect(getByText("This manual page doesn't exist. :(")).toBeInTheDocument();
});
