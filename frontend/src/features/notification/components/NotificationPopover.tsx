'use client';

import React, { useState, useEffect } from 'react';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { useNotificationStore } from '../store/use-notification-store';
import { Bell, Info, CheckCircle2, AlertTriangle, AlertOctagon, Trash2, Check } from 'lucide-react';
import { formatDistanceToNow } from 'date-fns';
import { enUS } from 'date-fns/locale';
import { cn } from '@/lib/utils';

type TabType = 'all' | 'unread';

export function NotificationPopover() {
  const {
    notifications,
    unreadCount,
    isLoading,
    fetchNotifications,
    deleteNotification,
  } = useNotificationStore();
  
  const [isOpen, setIsOpen] = useState(false);
  const [activeTab, setActiveTab] = useState<TabType>('all');
  
  // Track IDs that were unread before opening the popover to show visual indicators
  const [sessionUnreadIds, setSessionUnreadIds] = useState<string[]>([]);

  // Capture unread IDs when unreadCount increases
  useEffect(() => {
    if (unreadCount > 0) {
      const unreadItems = notifications.filter(n => !n.isRead).map(n => n.id);
      if (unreadItems.length > 0) {
        setSessionUnreadIds(prev => Array.from(new Set([...prev, ...unreadItems])));
      }
    }
  }, [unreadCount, notifications]);

  const handleOpenChange = (open: boolean) => {
    setIsOpen(open);
    if (open) {
      // Prior to fetching, mark the current unread items as part of this session's unread list
      const currentUnreads = notifications.filter(n => !n.isRead).map(n => n.id);
      if (currentUnreads.length > 0) {
        setSessionUnreadIds(currentUnreads);
      }
      fetchNotifications();
    } else {
      // Clear session unreads after popover closes and is read
      setSessionUnreadIds([]);
    }
  };

  const handleDelete = async (e: React.MouseEvent, id: string) => {
    e.stopPropagation();
    await deleteNotification(id);
    setSessionUnreadIds(prev => prev.filter(item => item !== id));
  };

  const formatTime = (dateStr: string) => {
    try {
      return formatDistanceToNow(new Date(dateStr), { addSuffix: true, locale: enUS });
    } catch (e) {
      return '';
    }
  };

  const getNotificationTypeConfig = (type: string) => {
    switch (type) {
      case 'SUCCESS':
        return {
          icon: CheckCircle2,
          bgClass: 'bg-[#28a745]/10',
          iconClass: 'text-[#28a745]',
          borderClass: 'border-[#28a745]/20',
        };
      case 'WARNING':
        return {
          icon: AlertTriangle,
          bgClass: 'bg-[#ffc107]/10',
          iconClass: 'text-[#d99b00]',
          borderClass: 'border-[#ffc107]/20',
        };
      case 'ERROR':
      case 'ALERT':
        return {
          icon: AlertOctagon,
          bgClass: 'bg-[#dc3545]/10',
          iconClass: 'text-[#dc3545]',
          borderClass: 'border-[#dc3545]/20',
        };
      case 'INFO':
      default:
        return {
          icon: Info,
          bgClass: 'bg-[#0066cc]/10',
          iconClass: 'text-[#0066cc]',
          borderClass: 'border-[#0066cc]/20',
        };
    }
  };

  // Filter notifications based on the selected tab
  // If 'unread' is selected, we filter by: either DB isRead is false, OR it is in our local sessionUnreadIds
  const filteredNotifications = notifications.filter((notification) => {
    if (activeTab === 'all') return true;
    return !notification.isRead || sessionUnreadIds.includes(notification.id);
  });

  // Count unreads for the current list
  const currentUnreadListCount = notifications.filter(
    n => !n.isRead || sessionUnreadIds.includes(n.id)
  ).length;

  return (
    <Popover open={isOpen} onOpenChange={handleOpenChange}>
      <PopoverTrigger asChild>
        <Button
          variant="ghost"
          size="sm"
          className="text-white hover:bg-white/10 relative h-9 w-9 p-0 flex items-center justify-center rounded-[4px] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-white focus-visible:ring-offset-2 focus-visible:ring-offset-blue-700"
        >
          <Bell className="h-5 w-5" />
          {unreadCount > 0 && (
            <span className="absolute -top-0.5 -right-0.5 h-4 min-w-[16px] rounded-full bg-[#dc3545] text-white font-semibold flex items-center justify-center text-[9px] px-1 animate-pulse border border-blue-600">
              {unreadCount > 99 ? '99+' : unreadCount}
            </span>
          )}
        </Button>
      </PopoverTrigger>

      <PopoverContent
        align="end"
        sideOffset={8}
        className="w-[380px] p-0 shadow-[0px_8px_20px_rgba(0,0,0,0.25)] border border-[#e0e0e0] rounded-[8px] bg-white overflow-hidden z-50 font-['Segoe_UI',_sans-serif] text-[#242424]"
      >
        {/* Header */}
        <div className="flex items-center justify-between py-3 px-4 border-b border-[#e0e0e0]/50 bg-[#f8f8f8]">
          <span className="text-[15px] font-semibold text-[#242424]">Notifications</span>
          {unreadCount > 0 && (
            <span className="text-[11px] text-[#0066cc] font-medium animate-pulse">
              New notifications
            </span>
          )}
        </div>

        {/* Tab switchers */}
        <div className="flex border-b border-[#e0e0e0]/40 px-2 bg-white">
          <button
            onClick={() => setActiveTab('all')}
            className={cn(
              "flex items-center space-x-1.5 py-2.5 px-3 text-[12px] font-medium border-b-2 transition-all duration-150 outline-none",
              activeTab === 'all'
                ? "border-[#0066cc] text-[#0066cc]"
                : "border-transparent text-[#898989] hover:text-[#242424]"
            )}
          >
            <span>All</span>
            <span className={cn(
              "px-1.5 py-0.2 rounded-full text-[10px] font-semibold",
              activeTab === 'all'
                ? "bg-[#0066cc]/10 text-[#0066cc]"
                : "bg-slate-100 text-[#898989]"
            )}>
              {notifications.length}
            </span>
          </button>
          <button
            onClick={() => setActiveTab('unread')}
            className={cn(
              "flex items-center space-x-1.5 py-2.5 px-3 text-[12px] font-medium border-b-2 transition-all duration-150 outline-none",
              activeTab === 'unread'
                ? "border-[#0066cc] text-[#0066cc]"
                : "border-transparent text-[#898989] hover:text-[#242424]"
            )}
          >
            <span>Unread</span>
            {currentUnreadListCount > 0 && (
              <span className={cn(
                "px-1.5 py-0.2 rounded-full text-[10px] font-semibold",
                activeTab === 'unread'
                  ? "bg-[#0066cc] text-white"
                  : "bg-[#dc3545] text-white animate-pulse"
              )}>
                {currentUnreadListCount}
              </span>
            )}
          </button>
        </div>

        {/* Notification List ScrollArea */}
        <ScrollArea className="h-[350px]">
          {isLoading ? (
            /* Skeleton Shimmer Loading list */
            <div className="divide-y divide-[#e0e0e0]/30">
              {[1, 2, 3].map((i) => (
                <div key={i} className="flex items-start space-x-3 p-4 animate-pulse">
                  <div className="flex-shrink-0 h-8 w-8 rounded-[4px] bg-slate-100" />
                  <div className="flex-1 space-y-2 py-0.5">
                    <div className="h-3.5 bg-slate-200 rounded w-2/3" />
                    <div className="h-3 bg-slate-150 rounded w-5/6" />
                    <div className="h-2.5 bg-slate-100 rounded w-1/4 pt-1" />
                  </div>
                </div>
              ))}
            </div>
          ) : filteredNotifications.length === 0 ? (
            /* Empty State */
            <div className="flex flex-col items-center justify-center py-16 px-6 text-center h-[340px]">
              <div className="h-12 w-12 rounded-full bg-[#f8f8f8] flex items-center justify-center text-[#898989]/50 mb-3.5 border border-[#e0e0e0]/40">
                {activeTab === 'unread' ? (
                  <Check className="h-5.5 w-5.5 text-[#28a745]" />
                ) : (
                  <Bell className="h-5.5 w-5.5" />
                )}
              </div>
              <p className="text-[13px] font-semibold text-[#242424]">
                {activeTab === 'unread' ? 'You have read all notifications' : 'No notifications found'}
              </p>
              <p className="text-[11px] text-[#898989] mt-1 max-w-[240px] leading-relaxed">
                {activeTab === 'unread'
                  ? 'Great! No unread notifications remaining.'
                  : 'You will be notified when there are new updates.'}
              </p>
            </div>
          ) : (
            <div className="divide-y divide-[#e0e0e0]/30 bg-white">
              {filteredNotifications.map((notification) => {
                const config = getNotificationTypeConfig(notification.type);
                const IconComponent = config.icon;
                const isUnread = !notification.isRead || sessionUnreadIds.includes(notification.id);

                return (
                  <div
                    key={notification.id}
                    className={cn(
                      "group flex items-start space-x-3 p-3.5 transition-colors duration-150 relative border-l-2",
                      isUnread
                        ? "border-l-[#0066cc] bg-[#f0f4ff]/15 hover:bg-[#f0f4ff]/25"
                        : "border-l-transparent hover:bg-slate-50"
                    )}
                  >
                    {/* Status Indicator Icon */}
                    <div
                      className={`flex-shrink-0 h-8 w-8 rounded-[4px] flex items-center justify-center ${config.bgClass} ${config.borderClass} border`}
                    >
                      <IconComponent className={`h-4.5 w-4.5 ${config.iconClass}`} />
                    </div>

                    {/* Content Area */}
                    <div className="flex-1 min-w-0 pr-6">
                      <div className="flex items-center justify-between">
                        <p className={cn(
                          "text-[13px] leading-snug break-words pr-2",
                          isUnread ? "font-bold text-[#242424]" : "font-semibold text-[#242424]"
                        )}>
                          {notification.title}
                        </p>
                        {/* Unread Blue Dot */}
                        {isUnread && (
                          <span className="h-1.5 w-1.5 rounded-full bg-[#0066cc] flex-shrink-0" />
                        )}
                      </div>
                      <p className="text-[12px] text-[#898989] mt-1 leading-relaxed break-words">
                        {notification.message}
                      </p>
                      <span className="text-[11px] text-[#898989]/85 mt-1.5 block">
                        {formatTime(notification.createdAt)}
                      </span>
                    </div>

                    {/* Delete Button */}
                    <button
                      onClick={(e) => handleDelete(e, notification.id)}
                      className="absolute right-2 top-2 p-1.5 rounded-[4px] text-[#898989] hover:text-[#dc3545] hover:bg-[#fff5f5] opacity-0 group-hover:opacity-100 focus:opacity-100 transition-all duration-150"
                      title="Delete notification"
                    >
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                );
              })}
            </div>
          )}
        </ScrollArea>
      </PopoverContent>
    </Popover>
  );
}
