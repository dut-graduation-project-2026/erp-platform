import * as React from "react";
import { useState, useEffect, useRef } from "react";
import { Input } from "./input";
import { getAutocomplete, GeocodingSearchResult } from "@/services/geocodingService";
import { MapPin, Loader2 } from "lucide-react";
import { cn } from "@/lib/utils";

export interface AddressInputProps extends Omit<React.ComponentProps<"input">, "onChange" | "value"> {
  value: string;
  onChange: (value: string) => void;
  onSelectAddress?: (address: GeocodingSearchResult) => void;
  focus?: string; // e.g. "10.75887508,106.67538868"
}

export const AddressInput = React.forwardRef<HTMLInputElement, AddressInputProps>(
  ({ className, value, onChange, onSelectAddress, focus, placeholder = "Enter address...", ...props }, ref) => {
    const [inputValue, setInputValue] = useState(value || "");
    const [suggestions, setSuggestions] = useState<GeocodingSearchResult[]>([]);
    const [isOpen, setIsOpen] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const containerRef = useRef<HTMLDivElement>(null);
    const debounceTimer = useRef<NodeJS.Timeout | null>(null);

    // Sync input value with external value prop change
    useEffect(() => {
      setInputValue(value || "");
    }, [value]);

    // Close suggestions dropdown when clicking outside
    useEffect(() => {
      const handleClickOutside = (event: MouseEvent) => {
        if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
          setIsOpen(false);
        }
      };
      document.addEventListener("mousedown", handleClickOutside);
      return () => {
        document.removeEventListener("mousedown", handleClickOutside);
      };
    }, []);

    const fetchSuggestions = async (text: string) => {
      if (!text.trim() || text.length < 2) {
        setSuggestions([]);
        setIsOpen(false);
        return;
      }

      setIsLoading(true);
      try {
        const results = await getAutocomplete(text, focus);
        setSuggestions(results || []);
        setIsOpen(results && results.length > 0);
      } catch (error) {
        console.error("Failed to fetch address suggestions", error);
      } finally {
        setIsLoading(false);
      }
    };

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const newValue = e.target.value;
      setInputValue(newValue);
      onChange(newValue);

      // Debounce suggestions fetch
      if (debounceTimer.current) {
        clearTimeout(debounceTimer.current);
      }

      debounceTimer.current = setTimeout(() => {
        fetchSuggestions(newValue);
      }, 800);
    };

    const handleSelectSuggestion = (suggestion: GeocodingSearchResult) => {
      const selectedValue = suggestion.display || suggestion.address || suggestion.name;
      setInputValue(selectedValue);
      onChange(selectedValue);
      setIsOpen(false);
      if (onSelectAddress) {
        onSelectAddress(suggestion);
      }
    };

    return (
      <div ref={containerRef} className="relative w-full">
        <div className="relative flex items-center">
          <Input
            ref={ref}
            type="text"
            value={inputValue}
            onChange={handleInputChange}
            onFocus={() => {
              if (suggestions.length > 0) setIsOpen(true);
            }}
            placeholder={placeholder}
            className={cn("pr-9", className)}
            {...props}
          />
          {isLoading && (
            <Loader2 className="absolute right-3 w-4 h-4 animate-spin text-muted-foreground" />
          )}
        </div>

        {isOpen && suggestions.length > 0 && (
          <div className="absolute left-0 right-0 z-50 mt-1 max-h-60 overflow-y-auto rounded-md border bg-popover p-1 text-popover-foreground shadow-md animate-in fade-in duration-100 bg-white">
            {suggestions.map((suggestion) => (
              <button
                key={suggestion.refId}
                type="button"
                onClick={() => handleSelectSuggestion(suggestion)}
                className="flex w-full items-start gap-2 rounded-sm px-2.5 py-2 text-left text-sm hover:bg-accent hover:text-accent-foreground transition-colors"
              >
                <MapPin className="mt-0.5 w-4 h-4 shrink-0 text-[#0066cc]" />
                <div className="flex flex-col overflow-hidden">
                  <span className="font-medium text-[#242424] truncate">
                    {suggestion.name || "Address"}
                  </span>
                  <span className="text-[12px] text-muted-foreground truncate">
                    {suggestion.address}
                  </span>
                </div>
              </button>
            ))}
          </div>
        )}
      </div>
    );
  }
);

AddressInput.displayName = "AddressInput";
