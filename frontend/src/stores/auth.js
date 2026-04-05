import { writable, derived } from 'svelte/store';

function createAuthStore() {
  const stored = typeof localStorage !== 'undefined' ? localStorage.getItem('user') : null;
  const initial = stored ? JSON.parse(stored) : null;

  const { subscribe, set, update } = writable(initial);

  return {
    subscribe,
    login: (user, token) => {
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
      set(user);
    },
    logout: () => {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      set(null);
    },
    set
  };
}

export const user = createAuthStore();
export const isLoggedIn = derived(user, $user => !!$user);
export const isSeller = derived(user, $user => $user?.role === 'SELLER');
export const isBuyer = derived(user, $user => $user?.role === 'BUYER');
export const isAdmin = derived(user, $user => $user?.role === 'ADMIN');
