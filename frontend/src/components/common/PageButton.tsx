import { Button, buttonVariants } from "@/components/ui/button";
import type { VariantProps } from "class-variance-authority";
import { cn } from "cn";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";

type PageButtonProps = {
  to: number;
  disabled?: boolean;
  size?: VariantProps<typeof buttonVariants>["size"];
  children: ReactNode;
};

export default function PageButton({
  to,
  disabled = false,
  size = "sm",
  children,
}: PageButtonProps) {
  return (
    <Button asChild variant="outline" size={size}>
      <Link
        to={`?page=${to}`}
        aria-disabled={disabled}
        onClick={(e) => disabled && e.preventDefault()}
        className={cn(disabled && "pointer-events-none opacity-50")}
      >
        {children}
      </Link>
    </Button>
  );
}
