// src/features/administration/components/RoleMatrix.tsx
// Role Permission Matrix Component - Define permissions for a role
// Follows DESIGN.md: Table pattern, checkboxes, proper spacing

import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Checkbox } from '@/components/ui/checkbox';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { getResources, getActions } from '@/services/mockPermissions';


interface RoleMatrixProps {
  selectedPermissions: string[];
  onPermissionsChange: (permissions: string[]) => void;
}

export const RoleMatrix: React.FC<RoleMatrixProps> = ({
  selectedPermissions,
  onPermissionsChange,
}) => {


  const handlePermissionToggle = (resource: string, action: string, checked: boolean) => {
    const permission = `${resource}:${action}`;
    const newPermissions = checked
      ? [...selectedPermissions, permission]
      : selectedPermissions.filter(p => p !== permission);

    onPermissionsChange(newPermissions);
  };

  const handleResourceToggle = (resource: string, checked: boolean) => {
    const actions = getActions();
    const resourcePermissions = actions.map(action => `${resource}:${action}`);

    const newPermissions = checked
      ? [...new Set([...selectedPermissions, ...resourcePermissions])]
      : selectedPermissions.filter(p => !resourcePermissions.includes(p));

    onPermissionsChange(newPermissions);
  };

  const handleActionToggle = (action: string, checked: boolean) => {
    const resources = getResources();
    const actionPermissions = resources.map(resource => `${resource}:${action}`);

    const newPermissions = checked
      ? [...new Set([...selectedPermissions, ...actionPermissions])]
      : selectedPermissions.filter(p => !actionPermissions.includes(p));

    onPermissionsChange(newPermissions);
  };

  const isResourceFullySelected = (resource: string) => {
    const actions = getActions();
    return actions.every(action => selectedPermissions.includes(`${resource}:${action}`));
  };

  const isActionFullySelected = (action: string) => {
    const resources = getResources();
    return resources.every(resource => selectedPermissions.includes(`${resource}:${action}`));
  };

  const resources = getResources();
  const actions = getActions();

  return (
    <Card className="shadow-standard">
      <CardHeader>
        <CardTitle className="text-lg font-semibold text-charcoal">
          Ma trận Quyền hạn
        </CardTitle>
        <p className="text-sm text-mid-gray">
          Chọn các quyền cho vai trò này. Hàng là Modules, cột là Actions.
        </p>
      </CardHeader>
      <CardContent>
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-light-gray border-b border-border-gray">
                <TableHead className="font-semibold text-charcoal sticky left-0 bg-light-gray z-10">
                  Modules / Actions
                </TableHead>
                {actions.map(action => (
                  <TableHead key={action} className="font-semibold text-charcoal text-center min-w-25">
                    <div className="flex flex-col items-center gap-2">
                      <span className="capitalize">{action}</span>
                      <Checkbox
                        checked={isActionFullySelected(action)}
                        onCheckedChange={(checked) => handleActionToggle(action, checked as boolean)}
                        className="data-[state=checked]:bg-primary-blue data-[state=checked]:border-primary-blue"
                      />
                    </div>
                  </TableHead>
                ))}
              </TableRow>
            </TableHeader>
            <TableBody>
              {resources.map(resource => (
                <TableRow key={resource} className="hover:bg-light-blue">
                  <TableCell className="font-medium text-charcoal sticky left-0 bg-white z-10 border-r border-border-gray">
                    <div className="flex items-center gap-3">
                      <Checkbox
                        checked={isResourceFullySelected(resource)}
                        onCheckedChange={(checked) => handleResourceToggle(resource, checked as boolean)}
                        className="data-[state=checked]:bg-primary-blue data-[state=checked]:border-primary-blue"
                      />
                      <span className="capitalize">{resource}</span>
                    </div>
                  </TableCell>
                  {actions.map(action => (
                    <TableCell key={action} className="text-center">
                      <Checkbox
                        checked={selectedPermissions.includes(`${resource}:${action}`)}
                        onCheckedChange={(checked) => handlePermissionToggle(resource, action, checked as boolean)}
                        className="data-[state=checked]:bg-primary-blue data-[state=checked]:border-primary-blue"
                      />
                    </TableCell>
                  ))}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>

        <div className="mt-4 p-4 bg-light-gray rounded-md">
          <h4 className="font-semibold text-charcoal mb-2">Quyền đã chọn:</h4>
          <div className="flex flex-wrap gap-2">
            {selectedPermissions.length === 0 ? (
              <span className="text-mid-gray">Chưa chọn quyền nào</span>
            ) : (
              selectedPermissions.map(permission => (
                <span
                  key={permission}
                  className="px-2 py-1 bg-primary-blue text-white text-xs rounded-md"
                >
                  {permission}
                </span>
              ))
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};
