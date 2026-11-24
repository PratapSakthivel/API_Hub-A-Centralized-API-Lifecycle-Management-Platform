import React from 'react';

export type UserRole = 'ADMIN' | 'DEVELOPER' | 'QA_ENGINEER' | 'PROJECT_MANAGER' | 'CONSUMER';

interface RoleGuardProps {
  allowedRoles: UserRole[];
  userRole?: UserRole;
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

export default function RoleGuard({
  allowedRoles,
  userRole = 'DEVELOPER', // Safe default context
  children,
  fallback = null,
}: RoleGuardProps) {
  const hasAccess = allowedRoles.includes(userRole);

  if (!hasAccess) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
}
