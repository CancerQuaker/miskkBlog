import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authApi, UserVO } from '../api/auth';
import { TOKEN_KEY } from '../api/client';

interface AuthState {
  token: string | null;
  user: UserVO | null;
  loginSuccess: (token: string, user: UserVO) => void;
  logout: () => void;
  refreshMe: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      token: null,
      user: null,
      loginSuccess: (token, user) => {
        localStorage.setItem(TOKEN_KEY, token);
        set({ token, user });
      },
      logout: () => {
        localStorage.removeItem(TOKEN_KEY);
        set({ token: null, user: null });
      },
      refreshMe: async () => {
        if (!get().token) return;
        try {
          const user = await authApi.me();
          set({ user });
        } catch {
          get().logout();
        }
      },
    }),
    {
      name: 'miskk-auth',
      partialize: (s) => ({ token: s.token, user: s.user }),
    },
  ),
);
