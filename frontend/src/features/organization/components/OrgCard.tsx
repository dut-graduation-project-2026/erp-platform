'use client';

import { Card, CardContent } from '@/components/ui/card';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Building2, MapPin, Phone } from 'lucide-react';

interface OrgCardProps {
  id: string;
  name: string;
  description?: string;
  hotline?: string;
  address?: string;
  role: string;
  onSelect: (orgId: string) => void;
}

export default function OrgCard({ id, name, description, hotline, address, role, onSelect }: OrgCardProps) {
  const initials = name
    .split(' ')
    .map(word => word.charAt(0).toUpperCase())
    .slice(0, 2)
    .join('');

  return (
    <Card className="cursor-pointer hover:shadow-lg transition-shadow duration-200 border border-gray-200">
      <CardContent className="p-6">
        <div className="flex items-start space-x-4">
          <Avatar className="h-12 w-12 bg-blue-100">
            <AvatarFallback className="text-blue-600 font-semibold">
              {initials}
            </AvatarFallback>
          </Avatar>

          <div className="flex-1 min-w-0">
            <h3 className="text-lg font-semibold text-gray-900 truncate">
              {name}
            </h3>
            {description && (
              <p className="text-sm text-gray-600 mt-1 line-clamp-2">
                {description}
              </p>
            )}
            <div className="flex items-center gap-2 mt-2">
              <Badge variant="secondary" className="text-xs">
                {role}
              </Badge>
            </div>
            {hotline && (
              <div className="flex items-center gap-1 mt-2 text-sm text-gray-500">
                <Phone className="h-3 w-3" />
                <span>{hotline}</span>
              </div>
            )}
            {address && (
              <div className="flex items-center gap-1 mt-1 text-sm text-gray-500">
                <MapPin className="h-3 w-3" />
                <span className="truncate">{address}</span>
              </div>
            )}
          </div>

          <Button
            onClick={() => onSelect(id)}
            className="bg-blue-600 hover:bg-blue-700 text-white shrink-0"
          >
            <Building2 className="h-4 w-4 mr-2" />
            Select
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}