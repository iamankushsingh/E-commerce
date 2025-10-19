import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class JwtService {
  private readonly SECRET_KEY = 'your-super-secret-jwt-key-change-this-in-production';
  private readonly TOKEN_EXPIRY = 24 * 60 * 60; // 24 hours in seconds

  constructor() { }

  // Create a JWT token
  createToken(payload: any): string {
    const header = this.base64UrlEncode(JSON.stringify({
      alg: 'HS256',
      typ: 'JWT'
    }));

    const now = Math.floor(Date.now() / 1000);
    const tokenPayload = {
      ...payload,
      iat: now, // Issued at
      exp: now + this.TOKEN_EXPIRY // Expiration time
    };

    const encodedPayload = this.base64UrlEncode(JSON.stringify(tokenPayload));
    const signature = this.createSignature(header, encodedPayload);

    return `${header}.${encodedPayload}.${signature}`;
  }

  // Verify and decode a JWT token
  verifyToken(token: string): any {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) {
        throw new Error('Invalid token format');
      }

      const [header, payload, signature] = parts;
      
      // Verify signature
      const expectedSignature = this.createSignature(header, payload);
      if (signature !== expectedSignature) {
        throw new Error('Invalid token signature');
      }

      // Decode payload
      const decodedPayload = JSON.parse(this.base64UrlDecode(payload));
      
      // Check expiration
      const currentTime = Math.floor(Date.now() / 1000);
      if (decodedPayload.exp < currentTime) {
        throw new Error('Token has expired');
      }

      return decodedPayload;
    } catch (error) {
      console.error('Token verification failed:', error);
      return null;
    }
  }

  // Check if token is expired
  isTokenExpired(token: string): boolean {
    try {
      const payload = this.verifyToken(token);
      return !payload; // If verification fails, consider expired
    } catch {
      return true;
    }
  }

  // Get token expiration time
  getTokenExpiration(token: string): Date | null {
    try {
      const payload = this.verifyToken(token);
      return payload ? new Date(payload.exp * 1000) : null;
    } catch {
      return null;
    }
  }

  // Get time until token expires (in seconds)
  getTimeUntilExpiry(token: string): number {
    try {
      const payload = this.verifyToken(token);
      if (!payload) return 0;
      
      const currentTime = Math.floor(Date.now() / 1000);
      return Math.max(0, payload.exp - currentTime);
    } catch {
      return 0;
    }
  }

  // Create HMAC-SHA256 signature (simplified for demo)
  private createSignature(header: string, payload: string): string {
    // In a real app, you'd use a proper HMAC-SHA256 implementation
    // This is a simplified version for demonstration
    const data = `${header}.${payload}`;
    const hash = this.simpleHash(data + this.SECRET_KEY);
    return this.base64UrlEncode(hash);
  }

  // Simple hash function (for demo purposes)
  private simpleHash(str: string): string {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash; // Convert to 32-bit integer
    }
    return hash.toString();
  }

  // Base64 URL encoding
  private base64UrlEncode(str: string): string {
    return btoa(str)
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=/g, '');
  }

  // Base64 URL decoding
  private base64UrlDecode(str: string): string {
    str = str.replace(/-/g, '+').replace(/_/g, '/');
    while (str.length % 4) {
      str += '=';
    }
    return atob(str);
  }

  // Generate a random secret key (for development)
  generateSecretKey(): string {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    let result = '';
    for (let i = 0; i < 64; i++) {
      result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return result;
  }
} 