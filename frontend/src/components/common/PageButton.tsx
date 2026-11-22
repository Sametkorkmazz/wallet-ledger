import { Button } from "@/components/ui/button";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";

type PageButtonProps = {
  to: number;
  disabled: boolean;
  children: ReactNode;
};

export default function PageButton({
  to,
  disabled,
  children,
}: PageButtonProps) {
  return disabled ? (
    <Button variant="outline" size="sm" disabled>
      {children}
    </Button>
  ) : (
    <Button asChild variant="outline" size="sm">
      <Link to={`?page=${to}`}>{children}</Link>
    </Button>
  );
}
