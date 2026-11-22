import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "cn";

export default function SkeletonText({ className }: { className?: string }) {
  return (
    <div className="flex h-lh items-center">
      <Skeleton className={cn("h-3", className)} />
    </div>
  );
}
