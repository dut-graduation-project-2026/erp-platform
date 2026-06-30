"use client";

import React from "react";
import { ChevronLeft, ChevronRight } from "lucide-react";
import { cn } from "@/lib/utils";

interface TablePaginationProps {
  page: number;
  limit: number;
  totalItems: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  onLimitChange?: (limit: number) => void;
  className?: string;
}

export function TablePagination({
  page,
  limit,
  totalItems,
  totalPages,
  onPageChange,
  onLimitChange,
  className,
}: TablePaginationProps) {
  if (totalItems === 0) return null;

  const startItem = (page - 1) * limit + 1;
  const endItem = Math.min(page * limit, totalItems);

  const getPageNumbers = () => {
    const pages: (number | string)[] = [];
    if (totalPages <= 7) {
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      // Always show first page
      pages.push(1);

      if (page > 4) {
        pages.push("...");
      }

      const start = Math.max(2, page - 2);
      const end = Math.min(totalPages - 1, page + 2);

      for (let i = start; i <= end; i++) {
        pages.push(i);
      }

      if (page < totalPages - 3) {
        pages.push("...");
      }

      // Always show last page
      pages.push(totalPages);
    }
    return pages;
  };

  return (
    <div
      className={cn(
        "mt-4 bg-white border border-[#e0e0e0] rounded-[4px] px-4 py-3 flex items-center justify-between text-[13px] text-[#64748b] shrink-0 shadow-sm",
        className
      )}
    >
      <div className="flex items-center gap-4">
        {onLimitChange && (
          <div className="flex items-center gap-1.5">
            <span className="text-[12px] text-[#898989] font-[600]">Rows per page:</span>
            <select
              value={limit}
              onChange={(e) => {
                onLimitChange(Number(e.target.value));
              }}
              className="h-8 border border-[#d0d0d0] rounded-[4px] px-2 text-[13px] bg-white focus:outline-none focus:border-[#0066cc]"
            >
              <option value={5}>5</option>
              <option value={10}>10</option>
              <option value={20}>20</option>
              <option value={50}>50</option>
              <option value={100}>100</option>
            </select>
          </div>
        )}
        <span>
          Showing {startItem} to {endItem} of {totalItems} items
        </span>
      </div>
      <div className="flex items-center gap-1">
        <button
          onClick={() => onPageChange(Math.max(1, page - 1))}
          disabled={page === 1}
          className="p-1 hover:bg-[#f8f8f8] hover:text-[#242424] rounded disabled:opacity-50 disabled:hover:bg-transparent transition-colors"
        >
          <ChevronLeft className="w-4 h-4" />
        </button>
        <div className="flex items-center gap-1">
          {getPageNumbers().map((pageNum, i) => {
            if (pageNum === "...") {
              return (
                <span key={`ellipsis-${i}`} className="w-7 h-7 flex items-center justify-center text-[#898989]">
                  ...
                </span>
              );
            }
            return (
              <button
                key={pageNum}
                className={cn(
                  "w-7 h-7 rounded flex items-center justify-center font-medium transition-colors",
                  page === pageNum
                    ? "bg-[#0066cc] text-white"
                    : "hover:bg-[#f8f8f8] text-[#242424]"
                )}
                onClick={() => onPageChange(pageNum as number)}
              >
                {pageNum}
              </button>
            );
          })}
        </div>
        <button
          onClick={() => onPageChange(Math.min(totalPages, page + 1))}
          disabled={page === totalPages}
          className="p-1 hover:bg-[#f8f8f8] hover:text-[#242424] rounded disabled:opacity-50 disabled:hover:bg-transparent transition-colors"
        >
          <ChevronRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
