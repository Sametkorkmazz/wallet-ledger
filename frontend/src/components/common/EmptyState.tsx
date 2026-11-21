import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyMedia,
  EmptyTitle,
} from "@/components/ui/empty";
import { Inbox } from "lucide-react";
import { useId } from "react";

export default function EmptyState({
  title,
  description,
}: {
  title: string;
  description: string;
}) {
  const id = useId();
  return (
    <Empty aria-labelledby={id}>
      <EmptyHeader>
        <EmptyMedia
          variant="icon"
          className="size-12 rounded-full bg-primary/12 text-primary"
        >
          <Inbox className="size-6" />
        </EmptyMedia>
        <EmptyTitle id={id}>{title}</EmptyTitle>
        <EmptyDescription>{description}</EmptyDescription>
      </EmptyHeader>
    </Empty>
  );
}
