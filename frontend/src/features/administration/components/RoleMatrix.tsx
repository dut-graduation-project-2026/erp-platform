// src/features/administration/components/RoleMatrix.tsx
// Role Permission Matrix Component - Define permissions for a role
// Follows DESIGN.md: Table pattern, checkboxes, proper spacing

import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Checkbox } from '@/components/ui/checkbox';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { getResources, getActions } from '@/services/mockPermissions';

import { PERMISSION_GROUPS, RESOURCE_LABELS } from '@/config/permissions';

interface RoleMatrixProps {
  selectedPermissions: string[];
  onPermissionsChange: (permissions: string[]) => void;
}

export const RoleMatrix: React.FC<RoleMatrixProps> = ({
  selectedPermissions,
  onPermissionsChange,
}) => {
  const [activeTab, setActiveTab] = useState<string>("crm");

  const activeGroup = PERMISSION_GROUPS.find(g => g.id === activeTab) || PERMISSION_GROUPS[0];
  const allResources = getResources();
  const visibleResources = activeGroup.resources.filter(res => allResources.includes(res));
  const actions = getActions();

  const handlePermissionToggle = (resource: string, action: string, checked: boolean) => {
    const permission = `${resource}:${action}`;
    const newPermissions = checked
      ? [...selectedPermissions, permission]
      : selectedPermissions.filter(p => p !== permission);

    onPermissionsChange(newPermissions);
  };

  const handleResourceToggle = (resource: string, checked: boolean) => {
    const resourcePermissions = actions.map(action => `${resource}:${action}`);

    const newPermissions = checked
      ? [...new Set([...selectedPermissions, ...resourcePermissions])]
      : selectedPermissions.filter(p => !resourcePermissions.includes(p));

    onPermissionsChange(newPermissions);
  };

  const handleActionToggle = (action: string, checked: boolean) => {
    const actionPermissions = visibleResources.map(resource => `${resource}:${action}`);

    const newPermissions = checked
      ? [...new Set([...selectedPermissions, ...actionPermissions])]
      : selectedPermissions.filter(p => !actionPermissions.includes(p));

    onPermissionsChange(newPermissions);
  };

  const isResourceFullySelected = (resource: string) => {
    return actions.every(action => selectedPermissions.includes(`${resource}:${action}`));
  };

  const isActionFullySelected = (action: string) => {
    if (visibleResources.length === 0) return false;
    return visibleResources.every(resource => selectedPermissions.includes(`${resource}:${action}`));
  };

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
        {/* Tabs Header */}
        <div className="flex border-b border-border-gray mb-6 overflow-x-auto no-scrollbar gap-2">
          {PERMISSION_GROUPS.map((group) => (
            <button
              key={group.id}
              type="button"
              onClick={() => setActiveTab(group.id)}
              className={`px-4 py-2 text-sm font-semibold whitespace-nowrap transition-colors border-b-2 -mb-[1px] ${
                activeTab === group.id
                  ? "text-primary-blue border-primary-blue"
                  : "text-mid-gray border-transparent hover:text-charcoal"
              }`}
            >
              {group.name}
            </button>
          ))}
        </div>

        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-light-gray border-b border-border-gray">
                <TableHead className="font-semibold text-charcoal sticky left-0 bg-light-gray z-10 w-[280px]">
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
              {visibleResources.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={actions.length + 1} className="py-8 text-center text-mid-gray text-[13px]">
                    Không có tài nguyên nào trong phân hệ này.
                  </TableCell>
                </TableRow>
              ) : (
                visibleResources.map(resource => (
                  <TableRow key={resource} className="hover:bg-light-blue">
                    <TableCell className="font-medium text-charcoal sticky left-0 bg-white z-10 border-r border-border-gray">
                      <div className="flex items-center gap-3">
                        <Checkbox
                          checked={isResourceFullySelected(resource)}
                          onCheckedChange={(checked) => handleResourceToggle(resource, checked as boolean)}
                          className="data-[state=checked]:bg-primary-blue data-[state=checked]:border-primary-blue"
                        />
                        <span>{RESOURCE_LABELS[resource] || resource}</span>
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
                ))
              )}
            </TableBody>
          </Table>
        </div>

        <div className="mt-6 p-4 bg-light-gray rounded-md">
          <h4 className="font-semibold text-charcoal mb-2">Quyền đã chọn ({selectedPermissions.length}):</h4>
          <div className="flex flex-wrap gap-2 max-h-32 overflow-y-auto p-1">
            {selectedPermissions.length === 0 ? (
              <span className="text-mid-gray text-sm">Chưa chọn quyền nào</span>
            ) : (
              selectedPermissions.map(permission => (
                <span
                  key={permission}
                  className="px-2 py-1 bg-primary-blue text-white text-[11px] rounded-[4px] font-mono"
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
