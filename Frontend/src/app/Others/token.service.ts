// token.service.ts
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TokenService {

  // Decode JWT (without external libraries)
  private decodeToken(token: string): any {
    try {
      const payload = token.split('.')[1];
      // Add padding if needed
      const normalizedPayload = payload.replace(/-/g, '+').replace(/_/g, '/');
      const paddings = normalizedPayload.length % 4;
      const paddedPayload = paddings ? normalizedPayload + '='.repeat(4 - paddings) : normalizedPayload;
      
      return JSON.parse(atob(paddedPayload));
    } catch (e) {
      console.error('Invalid JWT token', e);
      return null;
    }
  }

  // Get expiration date from token
  getTokenExpirationDate(token?: string): Date | null {
    const jwt = token || localStorage.getItem('token');
    if (!jwt) return null;

    const payload = this.decodeToken(jwt);
    if (!payload || !payload.exp) return null;

    return new Date(payload.exp * 1000); // exp is in seconds
  }

  // Get remaining time as a nice string (e.g., "12 minutes 34 seconds")
  getTimeLeft(token?: string): string {
    const expiresAt = this.getTokenExpirationDate(token);
    if (!expiresAt) return 'Invalid or missing token';

    const now = new Date();
    const diffMs = expiresAt.getTime() - now.getTime();

    if (diffMs <= 0) {
      return 'Token already expired';
    }

    const diffSec = Math.floor(diffMs / 1000);
    const minutes = Math.floor(diffSec / 60);
    const seconds = diffSec % 60;

    if (minutes > 60) {
      const hours = Math.floor(minutes / 60);
      const mins = minutes % 60;
      return `${hours} hour${hours > 1 ? 's' : ''} ${mins} minute${mins > 1 ? 's' : ''}`;
    }

    return `${minutes} minute${minutes !== 1 ? 's' : ''} ${seconds} second${seconds !== 1 ? 's' : ''}`;
  }

  // Check if token is expired
  isTokenExpired(token?: string): boolean {
    const expiresAt = this.getTokenExpirationDate(token);
    if (!expiresAt) return true;
    return new Date() >= expiresAt;
  }

  // Get all token info (great for debugging)
  getTokenInfo(token?: string): any {
    const jwt = token || localStorage.getItem('token');
    if (!jwt) return { error: 'No token found' };

    const payload = this.decodeToken(jwt);
    const expiresAt = this.getTokenExpirationDate(jwt);

    return {
      token: jwt,
      payload: payload,
      issuedAt: payload?.iat ? new Date(payload.iat * 1000).toLocaleString() : 'N/A',
      expiresAt: expiresAt ? expiresAt.toLocaleString() : 'N/A',
      timeLeft: this.getTimeLeft(jwt),
      isExpired: this.isTokenExpired(jwt),
      rawExp: payload?.exp || null
    };
  }
}