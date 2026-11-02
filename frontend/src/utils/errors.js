export function getErrorMessage(error) {
  const response = error.response?.data;
  if (response?.errors && Object.keys(response.errors).length > 0) {
    return Object.values(response.errors).join(' ');
  }
  if (response?.message) {
    return response.message;
  }
  if (error.code === 'ERR_NETWORK') {
    return 'Could not reach the server. Check that the backend is running.';
  }
  return 'Something went wrong. Please try again.';
}
