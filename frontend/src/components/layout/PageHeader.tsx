import { Skeleton } from "@/components/ui/skeleton";
import type { ReactNode } from "react";

type PageHeaderProps = {
  title: string;
  description?: ReactNode;
  action?: ReactNode;
  loading?: boolean;
};

export default function PageHeader({
  title,
  description,
  action,
  loading,
}: PageHeaderProps) {
  return (
    <header className="flex items-start justify-between">
      <div className="flex flex-col gap-1">
        <h1 className="page-title">{title}</h1>
        {loading ? (
          <div className="caption">
            <Skeleton className="h-lh w-48" />
          </div>
        ) : (
          description && <p className="caption">{description}</p>
        )}
      </div>
      {action}
    </header>
  );
}
